package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public final class Clients {

    private Clients() {
    }

    public record Quote(String symbol, BigDecimal price) {
    }

    public record HoldingState(String symbol, int shares) {
    }

    public record UserState(UUID id, String username, BigDecimal cashBalance, List<HoldingState> holdings) {
    }

    public record ApplyRequest(String type, String symbol, Integer shares, BigDecimal price) {
    }

    @FeignClient(name = "market-service")
    public interface MarketClient {
        @GetMapping("/api/quotes/{symbol}")
        Quote quote(@PathVariable("symbol") String symbol);
    }

    @FeignClient(name = "user-service")
    public interface UserClient {
        @GetMapping("/internal/users/{id}")
        UserState get(@PathVariable("id") UUID id);

        @PostMapping("/internal/users/{id}/apply")
        UserState apply(@PathVariable("id") UUID id, @RequestBody ApplyRequest request);
    }
}
