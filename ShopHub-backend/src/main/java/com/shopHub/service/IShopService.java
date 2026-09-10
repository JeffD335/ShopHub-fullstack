package com.shopHub.service;

import com.shopHub.dto.Result;
import com.shopHub.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result updateShop(Shop shop);

    Result queryShopByTypeId(Integer typeId, Integer current, Double x, Double y);
}
