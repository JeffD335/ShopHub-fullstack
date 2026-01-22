package com.shopHub.service;

import com.shopHub.dto.Result;
import com.shopHub.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  Service Interface
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryAll();
}
