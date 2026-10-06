package com.juanesteban.tcc.finance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Holding {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private int shares;

    protected Holding() {}

    public Holding(UserAccount user, String symbol, int shares) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.symbol = symbol;
        this.shares = shares;
    }

    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
}
