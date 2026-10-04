package com.juanesteban.tcc.trade.dto;

import java.util.UUID;

public class TradeRequestDto {
    private UUID userId;
    private String symbol;
    private int shares;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
}
