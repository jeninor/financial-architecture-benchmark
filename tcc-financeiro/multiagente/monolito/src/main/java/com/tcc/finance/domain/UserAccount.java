package com.tcc.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    protected UserAccount() {
    }

    public UserAccount(String username, BigDecimal saldo) {
        this.username = username;
        this.saldo = saldo;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void debit(BigDecimal amount) {
        this.saldo = this.saldo.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.saldo = this.saldo.add(amount);
    }
}
