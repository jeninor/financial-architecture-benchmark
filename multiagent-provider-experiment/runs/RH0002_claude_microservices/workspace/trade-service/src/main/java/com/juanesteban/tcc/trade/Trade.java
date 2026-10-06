package com.juanesteban.tcc.trade;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String symbol;
    @Column(nullable = false)
    private int shares;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private BigDecimal total;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Trade() {}

    public Trade(UUID userId, String type, String symbol, int shares, BigDecimal price) {
        this.userId = userId;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.total = price.multiply(BigDecimal.valueOf(shares));
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
}
