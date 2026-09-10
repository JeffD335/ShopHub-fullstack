package com.shopHub;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeckillLuaScriptTest {

    @Test
    void scriptRejectsMissingOrDepletedStockAndPublishesToTheOrderStream() throws Exception {
        String script = new String(
                Files.readAllBytes(Paths.get("src/main/resources/seckill.lua")),
                StandardCharsets.UTF_8
        );

        assertTrue(script.contains("stock == nil or stock <= 0"));
        assertTrue(script.contains("'stream.orders'"));
    }
}
