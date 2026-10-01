package com.tcc.finance.trade.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Column(nullable = false)
    private Instant timestamp;

    protected Transacao() {
        // JPA
    }

    public Transacao(String username, String symbol, long quantity, BigDecimal preco, TipoTransacao tipo, Instant timestamp) {
        this.username = username;
        this.symbol = symbol;
        this.quantity = quantity;
        this.preco = preco;
        this.tipo = tipo;
        this.timestamp = timestamp;
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

    public BigDecimal getPreco() {
        return preco;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
