package com.shopHub.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopHub.dto.Result;
import com.shopHub.entity.Shop;
import com.shopHub.service.IShopService;
import com.shopHub.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * Frontend Controller
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    /**
     * Query shop information by id
     * @param id Shop id
     * @return Shop detail data
     */
    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /**
     * Add new shop information
     * @param shop Shop data
     * @return Shop id
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // Save to database
        shopService.save(shop);
        // Return shop id
        return Result.ok(shop.getId());
    }

    /**
     * Update shop information
     * @param shop Shop data
     * @return void
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // Save to database
        return shopService.updateShop(shop);
    }

    /**
     * Query shop information by shop type with pagination
     * @param typeId Shop type
     * @param current Page number
     * @return Shop list
     */
    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // Query by type with pagination
        Page<Shop> page = shopService.query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // Return data
        return Result.ok(page.getRecords());
    }

    /**
     * Query shop information by shop name keyword with pagination
     * @param name Shop name keyword
     * @param current Page number
     * @return Shop list
     */
    @GetMapping("/of/name")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // Query by name with pagination
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // Return data
        return Result.ok(page.getRecords());
    }
}
