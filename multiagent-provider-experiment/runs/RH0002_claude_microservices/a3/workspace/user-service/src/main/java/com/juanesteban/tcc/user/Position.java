package com.juanesteban.tcc.user;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "symbol"}))
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

    protected Position() {}

    public Position(UUID userId, String symbol, int shares) {
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public void setShares(int s) { this.shares = s; }
}
