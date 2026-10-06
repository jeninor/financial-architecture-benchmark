package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

public class Clients {

    @FeignClient(name = "user-service")
    public interface UserClient {
        @GetMapping("/internal/users/{id}")
        Map<String, Object> get(@PathVariable("id") String id);

        @PostMapping("/internal/users/{id}/apply")
        Map<String, Object> apply(@PathVariable("id") String id, @RequestBody Map<String, Object> body);
    }

    @FeignClient(name = "market-service")
    public interface MarketClient {
        @GetMapping("/api/quotes/{symbol}")
        Map<String, Object> quote(@PathVariable("symbol") String symbol);
    }
}
