package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "symbol"}))
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long shares;

    protected Holding() {
    }

    public Holding(UUID userId, String symbol, long shares) {
        this.userId = userId;
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public long getShares() { return shares; }
    public void setShares(long shares) { this.shares = shares; }
}
