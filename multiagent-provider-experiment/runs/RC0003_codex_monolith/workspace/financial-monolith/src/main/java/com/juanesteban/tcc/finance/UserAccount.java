package com.juanesteban.tcc.finance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class UserAccount {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    protected UserAccount() {}

    public UserAccount(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.cashBalance = new BigDecimal("10000.00");
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public void setCashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; }
}
