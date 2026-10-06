package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public final class Clients {

    private Clients() {}

    public record Quote(String symbol, BigDecimal price) {}

    public record Apply(String symbol, long shares, BigDecimal price) {}

    public record UserView(UUID id, BigDecimal cash) {}

    @FeignClient(name = "market-service")
    public interface MarketClient {
        @GetMapping("/api/quotes/{symbol}")
        Quote quote(@PathVariable("symbol") String symbol);
    }

    @FeignClient(name = "user-service")
    public interface UserClient {
        @GetMapping("/internal/users/{id}")
        UserView get(@PathVariable("id") String id);

        @PostMapping("/internal/users/{id}/buy")
        UserView buy(@PathVariable("id") String id, @RequestBody Apply apply);

        @PostMapping("/internal/users/{id}/sell")
        UserView sell(@PathVariable("id") String id, @RequestBody Apply apply);
    }
}
