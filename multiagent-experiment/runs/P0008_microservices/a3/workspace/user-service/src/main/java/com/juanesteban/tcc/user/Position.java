package com.juanesteban.tcc.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    protected Position() {
    }

    public Position(UUID userId, String symbol, int shares) {
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public UUID getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
}
