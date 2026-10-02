package com.tcc.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

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

    public Transacao(Usuario usuario, String symbol, long quantity, BigDecimal preco, TipoTransacao tipo, Instant timestamp) {
        this.usuario = usuario;
        this.symbol = symbol;
        this.quantity = quantity;
        this.preco = preco;
        this.tipo = tipo;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
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
