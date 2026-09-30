package com.juanesteban.tcc.trade.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "market-service"
)
public interface MarketClient {

    @GetMapping(
        "/api/quotes/{symbol}"
    )
    MarketQuoteResponse getQuote(

        @PathVariable("symbol")
        String symbol
    );
}