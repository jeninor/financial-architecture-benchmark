package com.juanesteban.tcc.user;

import jakarta.persistence.*;
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
