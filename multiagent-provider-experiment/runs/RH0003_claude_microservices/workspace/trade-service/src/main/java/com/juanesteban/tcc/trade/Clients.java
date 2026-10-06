package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "market-service")
interface MarketClient {

    @GetMapping("/api/quotes/{symbol}")
    Map<String, Object> quote(@PathVariable("symbol") String symbol);
}

@FeignClient(name = "user-service")
interface UserClient {

    record Apply(String type, String symbol, long shares, BigDecimal amount) {}

    @GetMapping("/internal/users/{id}")
    Map<String, Object> get(@PathVariable("id") UUID id);

    @PostMapping("/internal/users/{id}/apply")
    Map<String, Object> apply(@PathVariable("id") UUID id, @RequestBody Apply req);
}
