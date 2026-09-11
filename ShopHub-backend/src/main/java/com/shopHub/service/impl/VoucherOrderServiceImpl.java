package com.shopHub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.shopHub.dto.Result;
import com.shopHub.entity.VoucherOrder;
import com.shopHub.mapper.VoucherOrderMapper;
import com.shopHub.service.ISeckillVoucherService;
import com.shopHub.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.utils.RedisIdWorker;
import com.shopHub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    @Lazy
    private IVoucherOrderService voucherOrderService;

    private static final String ORDER_STREAM_KEY = "stream.orders";
    private static final String ORDER_GROUP = "g1";
    private static final String ORDER_CONSUMER = "c1";
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    private final ExecutorService seckillOrderExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "voucher-order-consumer");
        thread.setDaemon(true);
        return thread;
    });
    private volatile boolean running = false;
    private Future<?> orderHandlerTask;

    @PostConstruct
    public void init() {
        createConsumerGroupIfNecessary();
        running = true;
        orderHandlerTask = seckillOrderExecutor.submit(new VoucherOrderHandler());
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (orderHandlerTask != null) {
            orderHandlerTask.cancel(true);
        }
        seckillOrderExecutor.shutdownNow();
        try {
            if (!seckillOrderExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                log.warn("Voucher order consumer did not stop within timeout.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void createConsumerGroupIfNecessary() {
        try {
            stringRedisTemplate.opsForStream().createGroup(ORDER_STREAM_KEY, ReadOffset.latest(), ORDER_GROUP);
        } catch (RedisSystemException e) {
            if (isBusyGroup(e)) {
                return;
            }
            if (!isMissingStream(e)) {
                throw e;
            }
            stringRedisTemplate.opsForStream().add(ORDER_STREAM_KEY, Collections.singletonMap("init", "init"));
            try {
                stringRedisTemplate.opsForStream().createGroup(ORDER_STREAM_KEY, ReadOffset.latest(), ORDER_GROUP);
            } catch (RedisSystemException ex) {
                if (!isBusyGroup(ex)) {
                    throw ex;
                }
            }
        }
    }

    private boolean isBusyGroup(RedisSystemException e) {
        String message = e.getMessage();
        return message != null && message.contains("BUSYGROUP");
    }

    private boolean isMissingStream(RedisSystemException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        String lowerCaseMessage = message.toLowerCase();
        return lowerCaseMessage.contains("no such key") || lowerCaseMessage.contains("requires the key to exist");
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1. Get current user and reserve a globally unique order id.
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        // 2. Run Lua admission control in Redis.
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        if (result == null) {
            return Result.fail("Flash sale request failed");
        }
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "Insufficient stock!" : "Repeated orders are not allowed!");
        }
        return Result.ok(orderId);
    }
    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 1. Enforce one flash-sale voucher per user.
        Long userId = voucherOrder.getUserId();
        int count = query().eq("user_id", userId).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            log.error("User has already purchased this voucher.");
            return;
        }
        // 2. Deduct stock with an optimistic check.
        boolean update = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)
                .update();
        if (!update) {
            log.error("Insufficient stock");
            return;
        }
        // 3. Insert the order into MySQL.
        save(voucherOrder);

    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // 1. Read one order request from the Redis Stream.
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from(ORDER_GROUP, ORDER_CONSUMER),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(ORDER_STREAM_KEY, ReadOffset.lastConsumed())
                    );
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    // 2. Persist the order and ACK only after success.
                    MapRecord<String, Object, Object> entries = list.get(0);
                    Map<Object, Object> value = entries.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    handleVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge(ORDER_STREAM_KEY, ORDER_GROUP, entries.getId());
                } catch (Exception e) {
                    if (isShutdownSignal(e)) {
                        break;
                    }
                    log.error("Order handle exception", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // 1. Recover one unacknowledged order request from the pending list.
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from(ORDER_GROUP, ORDER_CONSUMER),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(ORDER_STREAM_KEY, ReadOffset.from("0"))
                    );
                    if (list == null || list.isEmpty()) {
                        break;
                    }
                    // 2. Persist the pending order and ACK only after success.
                    MapRecord<String, Object, Object> entries = list.get(0);
                    Map<Object, Object> value = entries.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    handleVoucherOrder(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge(ORDER_STREAM_KEY, ORDER_GROUP, entries.getId());
                } catch (Exception e) {
                    if (isShutdownSignal(e)) {
                        break;
                    }
                    log.error("Order handle exception", e);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        log.error("Pending-list retry interrupted", ex);
                        break;
                    }
                }
            }
        }
    }

    private boolean isShutdownSignal(Exception e) {
        if (!running || Thread.currentThread().isInterrupted()) {
            return true;
        }
        Throwable current = e;
        while (current != null) {
            if (current instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.error("Duplicate order request ignored.");
            return;
        }
        try {
            voucherOrderService.createVoucherOrder(voucherOrder);
        } finally {
            redisLock.unlock();
        }
    }
}
