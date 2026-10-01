package com.tcc.finance.trade.client;

import com.tcc.finance.trade.client.dto.QuoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "market-service")
public interface MarketClient {

    @GetMapping("/quote/{symbol}")
    QuoteDto quote(@PathVariable("symbol") String symbol);
}
