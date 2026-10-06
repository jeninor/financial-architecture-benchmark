package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "market-service")
interface MarketClient {
    @GetMapping("/api/quotes/{symbol}")
    Quote quote(@PathVariable("symbol") String symbol);

    record Quote(String symbol, BigDecimal price) {}
}
