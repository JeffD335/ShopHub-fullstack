package com.shopHub.service;

import com.shopHub.dto.Result;
import com.shopHub.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopTypeService extends IService<ShopType> {

    Result queryAll();
}
