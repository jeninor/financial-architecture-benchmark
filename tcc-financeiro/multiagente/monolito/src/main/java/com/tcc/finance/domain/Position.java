package com.tcc.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(nullable = false)
    private int quantity;

    protected Position() {
    }

    public Position(UserAccount user, String symbol) {
        this.user = user;
        this.symbol = symbol;
        this.quantity = 0;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void add(int amount) {
        this.quantity += amount;
    }

    public void remove(int amount) {
        this.quantity -= amount;
    }
}
