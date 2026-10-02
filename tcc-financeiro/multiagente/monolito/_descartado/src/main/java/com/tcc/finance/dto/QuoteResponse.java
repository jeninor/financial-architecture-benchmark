package com.tcc.finance.dto;

import java.math.BigDecimal;

public class QuoteResponse {
    private String symbol;
    private BigDecimal price;

    public QuoteResponse(String symbol, BigDecimal price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
