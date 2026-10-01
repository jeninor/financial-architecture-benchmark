package com.tcc.finance.trade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Posicao de um usuario em um simbolo. O usuario vive em outro servico (user-service),
 * por isso a referencia e feita pelo username, sem chave estrangeira.
 */
@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "symbol"}))
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(nullable = false)
    private int quantity;

    protected Position() {
    }

    public Position(String username, String symbol) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = 0;
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
