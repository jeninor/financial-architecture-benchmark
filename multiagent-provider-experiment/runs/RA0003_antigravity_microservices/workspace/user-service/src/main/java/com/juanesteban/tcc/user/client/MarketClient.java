package com.juanesteban.tcc.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;

@FeignClient(name = "market-service")
public interface MarketClient {
    @GetMapping("/api/quotes/{symbol}")
    QuoteResponse getQuote(@PathVariable("symbol") String symbol);

    class QuoteResponse {
        private String symbol;
        private BigDecimal price;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}
