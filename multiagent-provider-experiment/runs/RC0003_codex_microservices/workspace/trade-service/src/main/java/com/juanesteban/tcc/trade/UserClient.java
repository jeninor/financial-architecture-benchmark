package com.juanesteban.tcc.trade;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
interface UserClient {
    @GetMapping("/internal/users/{userId}")
    void get(@PathVariable("userId") UUID userId);

    @PostMapping("/internal/users/{userId}/trades")
    void apply(@PathVariable("userId") UUID userId, @RequestBody TradeMutation mutation);

    record TradeMutation(String type, String symbol, int shares) {}
}
