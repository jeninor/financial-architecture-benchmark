package com.juanesteban.tcc.trade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TradeResponseDto {
    private UUID id;
    private UUID userId;
    private String symbol;
    private int shares;
    private BigDecimal price;
    private String type;
    private LocalDateTime timestamp;

    public TradeResponseDto(UUID id, UUID userId, String symbol, int shares, BigDecimal price, String type, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.type = type;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public String getType() { return type; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
