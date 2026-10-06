package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trades")
public class Trade {
    @Id
    public UUID id;
    @Column(name = "user_id", nullable = false)
    public UUID userId;
    @Column(nullable = false)
    public String type;
    @Column(nullable = false)
    public String symbol;
    @Column(nullable = false)
    public long shares;
    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal price;
    @Column(nullable = false, precision = 19, scale = 2)
    public BigDecimal total;
    @Column(nullable = false)
    public Instant createdAt;
}
