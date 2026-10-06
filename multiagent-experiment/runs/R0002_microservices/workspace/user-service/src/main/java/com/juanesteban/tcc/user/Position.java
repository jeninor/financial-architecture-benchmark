package com.juanesteban.tcc.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long shares;

    protected Position() {}

    public Position(UUID userId, String symbol, long shares) {
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public UUID getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public void setShares(long shares) { this.shares = shares; }
}
