package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "positions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Position {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long shares;

    protected Position() {
    }

    public Position(UUID userId, String symbol, long shares) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public void setShares(long shares) { this.shares = shares; }
}
