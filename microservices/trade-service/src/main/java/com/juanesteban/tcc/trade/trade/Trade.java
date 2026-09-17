package com.juanesteban.tcc.trade.trade;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(
        strategy = GenerationType.UUID
    )
    private UUID id;


    @Column(
        name = "user_id",
        nullable = false
    )
    private UUID userId;


    @Column(
        nullable = false,
        length = 20
    )
    private String symbol;


    @Enumerated(
        EnumType.STRING
    )
    @Column(
        name = "trade_type",
        nullable = false,
        length = 10
    )
    private TradeType type;


    @Column(nullable = false)
    private Integer shares;


    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal price;


    @Column(
        name = "created_at",
        nullable = false
    )
    private LocalDateTime createdAt;


    public Trade() {
    }


    public Trade(
        UUID userId,
        String symbol,
        TradeType type,
        Integer shares,
        BigDecimal price
    ) {

        this.userId = userId;
        this.symbol = symbol;
        this.type = type;
        this.shares = shares;
        this.price = price;

        this.createdAt =
            LocalDateTime.now();
    }


    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public TradeType getType() {
        return type;
    }

    public Integer getShares() {
        return shares;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}