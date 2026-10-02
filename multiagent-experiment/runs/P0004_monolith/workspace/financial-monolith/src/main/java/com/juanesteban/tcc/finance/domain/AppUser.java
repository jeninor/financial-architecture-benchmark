package com.juanesteban.tcc.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cash;

    protected AppUser() {
    }

    public AppUser(String username, BigDecimal cash) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cash = cash;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCash() { return cash; }
    public void setCash(BigDecimal cash) { this.cash = cash; }
}
