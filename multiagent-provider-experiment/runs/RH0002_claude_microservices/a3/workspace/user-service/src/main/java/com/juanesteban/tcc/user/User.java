package com.juanesteban.tcc.user;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class User {
    @Id
    private UUID id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private BigDecimal cashBalance;

    protected User() {}

    public User(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cashBalance = new BigDecimal("10000.00");
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal c) { this.cashBalance = c; }
}
