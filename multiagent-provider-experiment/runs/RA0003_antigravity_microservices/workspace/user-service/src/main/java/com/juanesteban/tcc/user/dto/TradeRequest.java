package com.juanesteban.tcc.user.dto;

import java.math.BigDecimal;

public class TradeRequest {
    private String symbol;
    private int shares;
    private BigDecimal price;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
