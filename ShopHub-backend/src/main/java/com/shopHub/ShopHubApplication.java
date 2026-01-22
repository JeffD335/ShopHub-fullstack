package com.shopHub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.shopHub.mapper")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class ShopHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopHubApplication.class, args);
    }

}
