package com.tcc.finance.trade.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "posicoes", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "symbol"}))
public class Posicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long quantity;

    protected Posicao() {
        // JPA
    }

    public Posicao(String username, String symbol, long quantity) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }
}
