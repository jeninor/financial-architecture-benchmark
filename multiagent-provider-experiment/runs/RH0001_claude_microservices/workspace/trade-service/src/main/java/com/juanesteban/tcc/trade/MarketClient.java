package com.juanesteban.tcc.trade;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "market-service")
public
interface MarketClient {
    record Quote(String symbol, BigDecimal price) {}

    @GetMapping("/api/quotes/{symbol}")
    Quote quote(@PathVariable("symbol") String symbol);
}
