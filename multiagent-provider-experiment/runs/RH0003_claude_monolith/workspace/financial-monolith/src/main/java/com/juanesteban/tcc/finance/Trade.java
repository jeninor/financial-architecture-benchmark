package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long shares;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Trade() {
    }

    public Trade(UUID userId, String type, String symbol, long shares, BigDecimal price) {
        this.userId = userId;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.total = price.multiply(BigDecimal.valueOf(shares));
    }

    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
}
