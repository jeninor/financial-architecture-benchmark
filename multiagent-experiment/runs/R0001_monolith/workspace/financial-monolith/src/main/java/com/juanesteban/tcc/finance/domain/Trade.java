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
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private long shares;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private Instant createdAt;

    protected Trade() {
    }

    public Trade(UUID userId, String symbol, Type type, long shares, BigDecimal price) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.total = price.multiply(BigDecimal.valueOf(shares));
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public Type getType() { return type; }
    public long getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
}
