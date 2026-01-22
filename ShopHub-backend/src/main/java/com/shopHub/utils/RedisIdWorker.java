package com.shopHub.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Redis implements globally unique IDs
 *
 */
@Component
public class RedisIdWorker {
    //begin time stamp is 2025/01/11 00:00:00
    private static final long BEGIN_TIMESTAMP = 1736553600L;
    private static  final int COUNT_BITS = 32;
    private StringRedisTemplate stringRedisTemplate;


    public RedisIdWorker(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public long nextId(String keyPrefix) {
        //1. generate time stamp
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        //2. generate serial number
        //2.1 get today's date
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //2.2 self increment
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        //3. append and return
        return timestamp << COUNT_BITS | count;
    }
}
