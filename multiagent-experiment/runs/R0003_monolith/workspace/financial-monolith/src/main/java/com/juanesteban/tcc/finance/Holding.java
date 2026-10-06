package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "holdings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
public class Holding {
    @Id
    public UUID id;
    @Column(name = "user_id", nullable = false)
    public UUID userId;
    @Column(nullable = false)
    public String symbol;
    @Column(nullable = false)
    public long shares;
}
