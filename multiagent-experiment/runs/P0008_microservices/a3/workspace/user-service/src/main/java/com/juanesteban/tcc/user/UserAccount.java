package com.juanesteban.tcc.user;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    protected UserAccount() {
    }

    public UserAccount(String username, BigDecimal cashBalance) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cashBalance = cashBalance;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }
}
