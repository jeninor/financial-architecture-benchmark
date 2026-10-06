package com.juanesteban.tcc.trade;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class TradeRecord {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long shares;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected TradeRecord() {}

    public TradeRecord(UUID userId, String type, String symbol, long shares, BigDecimal price) {
        this.userId = userId;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }
}
