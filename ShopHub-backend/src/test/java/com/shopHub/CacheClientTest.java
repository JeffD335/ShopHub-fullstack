package com.shopHub;

import com.shopHub.utils.CacheClient;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheClientTest {

    @Test
    void queryWithPassThroughUsesTheProvidedKeyPrefix() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:1")).thenReturn(null);

        CacheClient cacheClient = new CacheClient(redisTemplate);

        String result = cacheClient.queryWithPassThrough(
                "user:",
                1L,
                String.class,
                id -> "cached-user",
                10L,
                TimeUnit.MINUTES
        );

        assertEquals("cached-user", result);
        verify(valueOperations).set(eq("user:1"), contains("cached-user"), eq(10L), eq(TimeUnit.MINUTES));
    }
}
