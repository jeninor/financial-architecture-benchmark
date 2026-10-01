package com.tcc.finance.trade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = @Index(columnList = "username"))
public class TradeTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private TransactionType type;

    @Column(nullable = false, length = 16)
    private String symbol;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant timestamp;

    protected TradeTransaction() {
    }

    public TradeTransaction(String username, TransactionType type, String symbol, int quantity,
                            BigDecimal price, Instant timestamp) {
        this.username = username;
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public TransactionType getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
