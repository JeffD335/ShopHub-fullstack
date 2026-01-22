package com.shopHub.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shopHub.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.shopHub.utils.RedisConstants.*;

/**
 * encapsulate redis operation util class
 */
@Slf4j
@Component
public class CacheClient {
    private StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    //save into redis and set expire time
    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value) ,time, unit);
    }

    //save into redis and set logical expire time
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit){
        //encapsulate object into RedisData
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(time));

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallBack,
                                         Long time, TimeUnit unit){
        //1.query shop from redis cache
        String key = CACHE_SHOP_KEY + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.check if exist
        if(StrUtil.isNotBlank(json)){
            //3.exist, return
            return JSONUtil.toBean(json, type);
        }
        //either null(go to query DB) or not null(empty str)
        if(json != null){
            return null;
        }
        //4.not exist in cache, query from DB
        R r = dbFallBack.apply(id);
        //5.cannot find in DB, return
        if(r == null){
            //5.1 Cache null values to prevent cache penetration
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //6.found it, write into redis cache, set expiration time
        this.set(key, r ,time, unit);
        //7.return shop info
        return r;
    }
    private ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallBack, Long time, TimeUnit unit){
        //1.query from redis cache
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        //2.check if exist
        if(StrUtil.isBlank(json)){
            //3. not exist, return
            return null;
        }
        //4. exist, deserialize JSON to Object
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        JSONObject data = (JSONObject)redisData.getData();
        R r = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();

        //5.check if expired
        if(expireTime.isAfter(LocalDateTime.now())){
            //5.1 not expired, return r
            return r;
        }
        //5.2 expired, rebuild cache
        //5.3 try to get mutex lock
        String lockKey = LOCK_SHOP_KEY + id;
        boolean lock = getLocker(lockKey);
        //5.4 check if success of getting lock
        if (lock) {
            //5.6 success to get lock, start a thread, return old info
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    //6. rebuild cache ,new thread query from db
                    R newR = dbFallBack.apply(id);
                    //6.1 write into redis and set logical expire time
                    setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //7. release lock
                    releaseLocker(lockKey);
                }
            });

        }
        //8.return old info whether it get lock or not
        return r;
    }

    public boolean getLocker(String key){
        Boolean falg = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(falg);
    }
    public void releaseLocker(String key){
        stringRedisTemplate.delete(key);
    }
}
