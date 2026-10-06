package com.juanesteban.tcc.trade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
class TradeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    UUID userId;

    @Column(nullable = false)
    String type;

    @Column(nullable = false)
    String symbol;

    @Column(nullable = false)
    int shares;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal total;

    @Column(nullable = false)
    Instant createdAt;

    protected TradeRecord() {}

    TradeRecord(UUID userId, String type, String symbol, int shares, BigDecimal price) {
        this.userId = userId;
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.total = price.multiply(BigDecimal.valueOf(shares));
        this.createdAt = Instant.now();
    }
}
