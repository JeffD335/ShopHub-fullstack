package com.shopHub.controller;

import com.shopHub.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Result health() {
        return Result.ok("OK");
    }
}
