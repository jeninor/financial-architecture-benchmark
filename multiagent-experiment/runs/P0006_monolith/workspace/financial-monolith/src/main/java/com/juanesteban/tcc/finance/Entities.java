package com.juanesteban.tcc.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Entities {

    private Entities() {}

    @Entity
    @Table(name = "users")
    public static class AppUser {
        @Id
        public UUID id;
        @Column(nullable = false, unique = true)
        public String username;
        @Column(nullable = false, precision = 19, scale = 2)
        public BigDecimal cashBalance;
    }

    @Entity
    @Table(name = "positions",
           uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
    public static class Position {
        @Id
        public UUID id;
        @Column(nullable = false)
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
        public UUID id;
        @Column(nullable = false)
        public UUID userId;
        @Column(nullable = false)
        public String symbol;
        @Column(nullable = false)
        public String type;
        @Column(nullable = false)
        public long shares;
        @Column(nullable = false, precision = 19, scale = 2)
        public BigDecimal price;
        @Column(nullable = false)
        public Instant createdAt;
    }
}
