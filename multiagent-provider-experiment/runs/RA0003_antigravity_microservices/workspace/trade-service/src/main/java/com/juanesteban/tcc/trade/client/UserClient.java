package com.juanesteban.tcc.trade.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/internal/users/{userId}")
    ResponseEntity<Void> checkUser(@PathVariable("userId") UUID userId);

    @PostMapping("/api/internal/users/{userId}/buy")
    ResponseEntity<Void> buy(@PathVariable("userId") UUID userId, @RequestBody TradeRequest request);

    @PostMapping("/api/internal/users/{userId}/sell")
    ResponseEntity<Void> sell(@PathVariable("userId") UUID userId, @RequestBody TradeRequest request);

    class TradeRequest {
        private String symbol;
        private int shares;
        private BigDecimal price;

        public TradeRequest(String symbol, int shares, BigDecimal price) {
            this.symbol = symbol;
            this.shares = shares;
            this.price = price;
        }

        public String getSymbol() { return symbol; }
        public int getShares() { return shares; }
        public BigDecimal getPrice() { return price; }
    }
}
