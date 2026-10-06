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
    private BigDecimal cash;

    protected UserAccount() {}

    public UserAccount(UUID id, String username, BigDecimal cash) {
        this.id = id;
        this.username = username;
        this.cash = cash;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCash() { return cash; }
    public void setCash(BigDecimal cash) { this.cash = cash; }
}
