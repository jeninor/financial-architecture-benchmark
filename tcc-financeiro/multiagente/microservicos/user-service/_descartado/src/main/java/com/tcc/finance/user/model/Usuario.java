package com.tcc.finance.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class Usuario {

    public static final BigDecimal SALDO_INICIAL = new BigDecimal("10000.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    protected Usuario() {
        // JPA
    }

    public Usuario(String username, BigDecimal saldo) {
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

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
