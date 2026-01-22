package com.shopHub.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shopHub.dto.Result;
import com.shopHub.entity.ShopType;
import com.shopHub.mapper.ShopTypeMapper;
import com.shopHub.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.shopHub.utils.RedisConstants.SHOP_TYPE_KEY;
import static com.shopHub.utils.RedisConstants.SHOP_TYPE_TTL;

/**
 * <p>
 *  Service Implementation Class
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public Result queryAll() {
        // 1. query type list from cache
        Set<String> shopTypeSet = stringRedisTemplate.opsForZSet().range(SHOP_TYPE_KEY, 0, -1);
        
        // 2. check if exist in cache
        if (shopTypeSet != null && !shopTypeSet.isEmpty()) {
            // 3. exist, convert JSON into list
            List<ShopType> shopTypeList = new ArrayList<>();
            for (String jsonStr : shopTypeSet) {
                ShopType shopType = JSONUtil.toBean(jsonStr, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }
        
        // 4. not exist in cache, query from DB
        QueryWrapper<ShopType> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        List<ShopType> shopTypeList = this.list(queryWrapper);
        
        // 5. does not exist in DB
        if (shopTypeList == null || shopTypeList.isEmpty()) {
            return Result.fail("shop types does not exist");
        }
        
        // 6. write into redis cache
        for (ShopType shopType : shopTypeList) {
            String jsonStr = JSONUtil.toJsonStr(shopType);
            stringRedisTemplate.opsForZSet().add(SHOP_TYPE_KEY, jsonStr, shopType.getSort());
        }
        
        // 7. return query result
        return Result.ok(shopTypeList);
    }
}
