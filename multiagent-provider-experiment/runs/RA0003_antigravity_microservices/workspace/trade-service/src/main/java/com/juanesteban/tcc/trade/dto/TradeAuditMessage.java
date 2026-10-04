package com.juanesteban.tcc.trade.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TradeAuditMessage {
    private UUID tradeId;
    private UUID userId;
    private String symbol;
    private int shares;
    private BigDecimal price;
    private String type;

    public TradeAuditMessage() {}

    public TradeAuditMessage(UUID tradeId, UUID userId, String symbol, int shares, BigDecimal price, String type) {
        this.tradeId = tradeId;
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.type = type;
    }

    public UUID getTradeId() { return tradeId; }
    public UUID getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public String getType() { return type; }
}
