package com.juanesteban.tcc.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Position {

    @Id
    @GeneratedValue
    private Long id;

    private UUID userId;
    private String symbol;
    private long shares;

    protected Position() {
    }

    public Position(UUID userId, String symbol, long shares) {
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public void setShares(long shares) { this.shares = shares; }
}
