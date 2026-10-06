package com.juanesteban.tcc.finance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant createdAt;

    protected Trade() {}

    public Trade(UserAccount user, String type, String symbol, int shares, BigDecimal price) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public BigDecimal getPrice() { return price; }
    public Instant getCreatedAt() { return createdAt; }
}
