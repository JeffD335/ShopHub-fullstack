package com.shopHub.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shopHub.dto.Result;
import com.shopHub.entity.Shop;
import com.shopHub.mapper.ShopMapper;
import com.shopHub.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.utils.CacheClient;
import com.shopHub.utils.RedisConstants;
import com.shopHub.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.shopHub.utils.RedisConstants.*;

/**
 * <p>
 *  Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
@Resource
private StringRedisTemplate stringRedisTemplate;
@Resource
private CacheClient cacheClient;
    @Override
    public Result queryById(Long id) {
        //cache penetration
        //Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //solve cache breakdown with mutex locks
        Shop shop = queryWithMutex(id);
        //solve cache breakdown with logical expire
        //Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shop);
    }

    public Shop queryWithMutex(Long id){
        //1.query shop from redis cache
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.check if exist
        if(StrUtil.isNotBlank(shopJson)){
            //3.exist, return
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //either null(go to query DB) or not null(empty str)
        if(shopJson != null){
            return null;
        }
        //4. not exist in cache, rebuild cache
        //4.1 try to get mutex lock
        String lockKey = "lock:shop:" + id;
        Shop shop = null;
        try {
            boolean isLock = getLocker(lockKey);
            //4.2 check if success to get mutex lock
            if(!isLock){
                //4.3 if failure, sleep and retry
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            //4.4 success, query from DB
            shop = getById(id);
            //5.cannot find in DB, return
            if(shop == null){
                //5.1 Cache null values to prevent cache penetration
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //6.found it, write into redis cache, set expiration time
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonPrettyStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            releaseLocker(lockKey);
        }
        //7.return shop info
        return shop;
    }


    public boolean getLocker(String key){
       Boolean falg = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(falg);
    }
    public void releaseLocker(String key){
        stringRedisTemplate.delete(key);
    }

    /**
     * first update database, then invalidate cache.
     * in case of stale reads.
     * @param shop
     * @return
     */
    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        Long id = shop.getId();
        if(id == null){
            return  Result.fail("shop id can not be null");
        }
        //1. update DB
        updateById(shop);
        //2. delete cache
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

    public void saveShopToRedis(Long id, long expireSeconds){
        //1.query shop info
        Shop shop = getById(id);
        //2.encapsulate logic expiration time
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.write into redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }
}
