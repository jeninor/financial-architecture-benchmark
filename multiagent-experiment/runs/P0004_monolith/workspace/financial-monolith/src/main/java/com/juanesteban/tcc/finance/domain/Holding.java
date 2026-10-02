package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Holding {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    protected Holding() {
    }

    public Holding(UUID userId, String symbol, int shares) {
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
}
