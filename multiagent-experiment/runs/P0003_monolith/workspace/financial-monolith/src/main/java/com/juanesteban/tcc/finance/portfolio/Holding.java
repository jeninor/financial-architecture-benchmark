package com.juanesteban.tcc.finance.portfolio;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "monolith_holdings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"})
)
public class Holding {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false)
    private long shares;

    protected Holding() {
    }

    public Holding(UUID userId, String symbol) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.shares = 0;
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

    public long getShares() {
        return shares;
    }

    public void setShares(long shares) {
        this.shares = shares;
    }
}
