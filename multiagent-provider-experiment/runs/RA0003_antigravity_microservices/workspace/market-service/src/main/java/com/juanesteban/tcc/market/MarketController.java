package com.juanesteban.tcc.market;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/quotes")
public class MarketController {

    private static final Map<String, BigDecimal> QUOTES = Map.of(
            "AAPL", new BigDecimal("200.00"),
            "MSFT", new BigDecimal("400.00"),
            "GOOGL", new BigDecimal("170.00"),
            "AMZN", new BigDecimal("190.00"),
            "NVDA", new BigDecimal("120.00")
    );

    @GetMapping("/{symbol}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable String symbol) {
        if (symbol == null || !QUOTES.containsKey(symbol.toUpperCase())) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(new QuoteResponse(symbol.toUpperCase(), QUOTES.get(symbol.toUpperCase())));
    }

    public static class QuoteResponse {
        private String symbol;
        private BigDecimal price;

        public QuoteResponse(String symbol, BigDecimal price) {
            this.symbol = symbol;
            this.price = price;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
