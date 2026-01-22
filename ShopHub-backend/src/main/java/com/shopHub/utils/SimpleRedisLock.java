package com.shopHub.utils;

import cn.hutool.core.lang.UUID;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class SimpleRedisLock implements ILock{
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final String keyPrefix = "lock:";
    private String name;
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    @Override
    public boolean tryLock(long timeoutSec) {
        // acquire current thread id
        Long threadId = Thread.currentThread().getId();
        // setNX
        Boolean isLock = stringRedisTemplate.opsForValue().setIfAbsent(keyPrefix + name, threadId + "", timeoutSec, TimeUnit.SECONDS);
        return isLock;
    }

    @Override
    public void unLock() {
        // call lua script
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(keyPrefix + name),
                ID_PREFIX + Thread.currentThread().getId()
        );
    }
}
