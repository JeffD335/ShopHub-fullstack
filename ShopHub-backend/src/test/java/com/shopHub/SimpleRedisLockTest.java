package com.shopHub;

import com.shopHub.utils.SimpleRedisLock;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimpleRedisLockTest {

    @Test
    void tryLockStoresAnOwnerTokenThatCanBeCheckedDuringUnlock() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("lock:order:1"), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        SimpleRedisLock lock = new SimpleRedisLock(redisTemplate, "order:1");

        assertTrue(lock.tryLock(10L));
        verify(valueOperations).setIfAbsent(
                eq("lock:order:1"),
                argThat(value -> value != null && value.contains("-")),
                eq(10L),
                eq(TimeUnit.SECONDS)
        );
    }
}
