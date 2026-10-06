package com.juanesteban.tcc.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cash;

    protected UserAccount() {
    }

    public UserAccount(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cash = new BigDecimal("10000.00");
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCash() { return cash; }
    public void setCash(BigDecimal cash) { this.cash = cash; }
}
