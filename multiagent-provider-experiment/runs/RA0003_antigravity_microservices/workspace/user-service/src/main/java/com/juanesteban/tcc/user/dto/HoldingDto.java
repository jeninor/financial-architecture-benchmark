package com.juanesteban.tcc.user.dto;

import java.math.BigDecimal;

public class HoldingDto {
    private String symbol;
    private int shares;
    private BigDecimal price;
    private BigDecimal marketValue;

    public HoldingDto(String symbol, int shares, BigDecimal price, BigDecimal marketValue) {
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.marketValue = marketValue;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getMarketValue() { return marketValue; }
}
