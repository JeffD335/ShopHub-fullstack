package com.shopHub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.shopHub.dto.Result;
import com.shopHub.entity.VoucherOrder;
import com.shopHub.mapper.VoucherOrderMapper;
import com.shopHub.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.utils.RedisIdWorker;
import com.shopHub.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 *  Service Implementation Class
 * </p>
 *
 * @author
 * @since
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private SeckillVoucherServiceImpl seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    private IVoucherOrderService proxy;
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    //    private BlockingQueue<VoucherOrder> orderTasks =new ArrayBlockingQueue<>(1024 * 1024);
//    private class VoucherOrderHandler implements Runnable{
//
//        @Override
//        public void run() {
//            while (true){
//                //1. get order info in blocking queue
//                try {
//                    VoucherOrder voucherOrder = orderTasks.take();
//                    //2. create order
//                    handleVoucherOrder(voucherOrder);
//                } catch (Exception e) {
//                    log.error("Order handle exception", e);
//                }
//            }
//        }
//    }
    String queueName = "Streams.order";
    @Override
    public Result seckillVoucher(Long voucherId) {
        //1. get userId via current thread
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        //2. run Lua script
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "Insufficient stock!" : "Repeated orders are not allowed!");
        }
        //3. r == 0, eligible for purchase, save to blocking queue

        //4. get proxy object in advance, which can be used in a sub-thread
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        //5. return order id
        return Result.ok(orderId);
    }
    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //1. query DB by userId by voucherId, ensure one customer can only get one flash sale voucher
        Long userId = voucherOrder.getUserId();
        int count = query().eq("userId", userId).eq("voucherId", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            log.error("You has already made a purchase!");
            return;
        }
        //2. sufficient, deduct inventory
        boolean update = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("Voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)     //optimistic lock
                .update();
        if (!update) {
            log.error("Insufficient stock");
            return;
        }
        //3. insert new order into DB
        save(voucherOrder);

    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    //1. read order info from message queue "XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAM streams.order"
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    //2. check if success
                    if (list == null || list.isEmpty()) {
                        continue;
                    }
                    //3. success, parse info from message
                    MapRecord<String, Object, Object> entries = list.get(0);
                    Map<Object, Object> value = entries.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    //4. create order
                    handleVoucherOrder(voucherOrder);
                    //5. ACK confirmation "SACK stream.order g1 id"
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", entries.getId());
                } catch (Exception e) {
                    log.error("Order handle exception", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    //1. read order info from Pending-list "XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAM streams.order"
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );
                    //2. check if success
                    if (list == null || list.isEmpty()) {
                        break;
                    }
                    //3. success, parse info from message
                    MapRecord<String, Object, Object> entries = list.get(0);
                    Map<Object, Object> value = entries.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    //4. create order
                    handleVoucherOrder(voucherOrder);
                    //5. ACK confirmation "SACK stream.order g1 id"
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", entries.getId());
                } catch (Exception e) {
                    log.error("Order handle exception", e);
                    try {
                        Thread.sleep(20);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
        private void handleVoucherOrder(VoucherOrder voucherOrder) {
            //1. using pessimistic lock handle insert operation
            Long userId = voucherOrder.getUserId();
            //7. new a distributed lock
            RLock redisLock = redissonClient.getLock("lock:order:" + userId);
            //7.1 try to get lock
            boolean isLock = redisLock.tryLock();
            if (!isLock) {
                log.error("Duplicate orders are not allowed.");
                return;
            }
            //As transaction of spring is in threadLocal, but here is multi-thread
            //IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            try {
                proxy.createVoucherOrder(voucherOrder);
            } finally {
                redisLock.unlock();
            }

        }
        /**
         * order a seckillVoucher (Flash sale voucher)
         *
         * @param voucherId
         */
//    @Override
//    public Result seckillVoucher(Long voucherId){
//        //1.query voucher
//       SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
//       if(seckillVoucher == null){
//           return Result.fail("Voucher does not exist!");
//       }
//        //2.check if Flash sale started and not finished
//        if((seckillVoucher.getBeginTime().isAfter(LocalDateTime.now()))){
//            return Result.fail("The flash sale has not yet begun.");
//        }
//        if(seckillVoucher.getEndTime().isBefore(LocalDateTime.now())){
//            return Result.fail("The flash sale has ended");
//        }
//        //4. date is valid, check if the voucher is sufficient
//        if(seckillVoucher.getStock() < 1){
//            //5. not sufficient return failure result
//            return Result.fail("Insufficient stock");
//        }
//        //6. using pessimistic lock handle insert operation
//        Long userId = UserHolder.getUser().getId();
//        //7. new a distributed lock
////        SimpleRedisLock lock = new SimpleRedisLock(stringRedisTemplate, "order:" + userId);
//        RLock lock = redissonClient.getLock("lock:order:" + userId);
//
//        //7.1 try to get lock
//        boolean isLock = lock.tryLock();
//        if (!isLock){
//            return Result.fail("Duplicate orders are not allowed.");
//        }
//        synchronized (userId.toString().intern()){ //intern() means returns a canonical (shared) copy of the string from the JVM’s String pool.
//            //spring handle transaction by using proxy
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        }
//    }

        /**
         * Sub-thread: create voucher order actually
         *
         * @param voucherOrder
         */

    }



