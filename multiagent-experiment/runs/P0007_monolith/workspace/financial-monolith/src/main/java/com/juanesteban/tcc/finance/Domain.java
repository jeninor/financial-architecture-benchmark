package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

class Domain {
    private Domain() {}
}

@Entity
@Table(name = "app_users")
class AppUser {
    @Id
    UUID id = UUID.randomUUID();
    @Column(unique = true, nullable = false)
    String username;
    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal cash;
}

@Entity
@Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_id", nullable = false)
    UUID userId;
    @Column(nullable = false)
    String symbol;
    @Column(nullable = false)
    int shares;
}

@Entity
@Table(name = "trades")
class TradeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_id", nullable = false)
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
    Instant createdAt = Instant.now();
}
