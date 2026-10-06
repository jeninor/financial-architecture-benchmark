package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    protected User() {
    }

    public User(String username, BigDecimal cashBalance) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cashBalance = cashBalance;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }
}
