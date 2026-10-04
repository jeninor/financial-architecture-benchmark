package com.juanesteban.tcc.user.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "symbol"})})
public class HoldingEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
}
