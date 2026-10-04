package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Domain {

    @Entity
    @Table(name = "app_users")
    public static class AppUser {
        @Id
        public UUID id;
        @Column(unique = true, nullable = false)
        public String username;
        @Column(nullable = false, precision = 19, scale = 2)
        public BigDecimal cash;
    }

    @Entity
    @Table(name = "positions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
    public static class Position {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        @Column(name = "user_id", nullable = false)
        public UUID userId;
        @Column(nullable = false)
        public String symbol;
        @Column(nullable = false)
        public long shares;
    }

    @Entity
    @Table(name = "trades")
    public static class Trade {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;
        @Column(name = "user_id", nullable = false)
        public UUID userId;
        @Column(nullable = false)
        public String symbol;
        @Column(nullable = false)
        public String type;
        @Column(nullable = false)
        public long shares;
        @Column(nullable = false, precision = 19, scale = 2)
        public BigDecimal price;
        @Column(nullable = false, precision = 19, scale = 2)
        public BigDecimal total;
        @Column(nullable = false)
        public Instant createdAt;
    }
}
