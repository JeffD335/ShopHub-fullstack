package com.shopHub;

import com.shopHub.service.impl.ShopServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Test
    public void testSaveShop(){
        shopService.saveShopToRedis(1L, 10L);
    }
}
