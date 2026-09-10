package com.shopHub.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopHub.dto.Result;
import com.shopHub.entity.Shop;
import com.shopHub.service.IShopService;
import com.shopHub.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;

    @GetMapping("/{id}")
    public Result queryShopById(@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // Save to database
        shopService.save(shop);
        // Return shop id
        return Result.ok(shop.getId());
    }

    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // Save to database
        return shopService.updateShop(shop);
    }

    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "x", required = false) Double x,
            @RequestParam(value = "y", required = false) Double y
    ) {
        return shopService.queryShopByTypeId(typeId, current, x, y);
    }

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
