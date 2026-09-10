package com.shopHub.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shopHub.dto.Result;
import com.shopHub.entity.Shop;
import com.shopHub.mapper.ShopMapper;
import com.shopHub.service.IShopService;
import com.shopHub.utils.CacheClient;
import com.shopHub.utils.RedisConstants;
import com.shopHub.utils.RedisData;
import com.shopHub.utils.SystemConstants;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.shopHub.utils.RedisConstants.*;

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
        boolean isLock = false;
        try {
            isLock = getLocker(lockKey);
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
            if (isLock) {
                releaseLocker(lockKey);
            }
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

    @Override
    public Result queryShopByTypeId(Integer typeId, Integer current, Double x, Double y) {
        //1. check if need to query by x y, if not query from DB
        if(x == null || y == null){
            Page<Shop> page = query().eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        //2. calculate pagination
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        //3. query from redis, sorting and paging by distance
        String key = SHOP_GEO_KEY + typeId;
        //GEORADIUS key x y 5 km WITHDIST COUNT end
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(key, new Circle(new Point(x, y), new Distance(5, Metrics.KILOMETERS)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(end));
        //4. parse id
        if(results == null) return Result.ok(Collections.emptyList());

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();
        //next page does not exist
        if(content.size() <= from) return  Result.ok(Collections.emptyList());
        //4.1 paging from end
        List<Long> ids = new ArrayList<>(content.size());
        Map<String, Distance> distanceMap = new HashMap<>(content.size());
        content.stream().skip(from).forEach(result ->{
                    //4.2 get shopId and store in ids List
                    String shopIdstr = result.getContent().getName();
                    ids.add(Long.valueOf(shopIdstr));
                    //4.3 get distance and put into Map
                    Distance distance = result.getDistance();
                    distanceMap.put(shopIdstr, distance);
                }
        );
        //5. query shop by id
        String idStr = StrUtil.join("," , ids);
        //WHERE id IN (5, 2, 9) ORDER BY FIELD(id, 5, 2, 9);
        List<Shop> shopList = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        shopList.forEach(
                shop -> shop.setDistance(distanceMap.get(shop.getId().toString()).getValue())
        );
        //6. return
        return Result.ok(shopList);
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
