package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    protected AppUser() {
    }

    public AppUser(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cashBalance = new BigDecimal("10000.00");
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }
}
