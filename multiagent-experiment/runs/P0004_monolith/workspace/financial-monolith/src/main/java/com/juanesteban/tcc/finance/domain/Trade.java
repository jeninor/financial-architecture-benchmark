package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {

    public enum Type { BUY, SELL }

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Trade() {
    }

    public Trade(UUID userId, Type type, String symbol, int shares, BigDecimal price) {
        this.userId = userId;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Type getType() { return type; }
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }
}
