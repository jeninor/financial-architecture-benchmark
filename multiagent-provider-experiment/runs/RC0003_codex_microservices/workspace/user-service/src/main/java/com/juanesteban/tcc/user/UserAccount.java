package com.juanesteban.tcc.user;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true)
    String username;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal cashBalance = new BigDecimal("10000.00");

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_holdings")
    @MapKeyColumn(name = "symbol")
    @Column(name = "shares", nullable = false)
    Map<String, Integer> holdings = new HashMap<>();

    protected UserAccount() {}

    UserAccount(String username) {
        this.username = username;
    }
}
