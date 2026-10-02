package com.juanesteban.tcc.finance.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monolith_trades")
public class Trade {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private TradeType type;

    @Column(nullable = false)
    private long shares;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    protected Trade() {
    }

    public Trade(UUID userId, String symbol, TradeType type, long shares, BigDecimal price) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.type = type;
        this.shares = shares;
        this.price = price;
        this.total = price.multiply(BigDecimal.valueOf(shares));
        this.executedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public TradeType getType() {
        return type;
    }

    public long getShares() {
        return shares;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
