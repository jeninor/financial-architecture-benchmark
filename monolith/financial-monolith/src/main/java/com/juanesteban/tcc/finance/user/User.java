package com.juanesteban.tcc.finance.user;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
        nullable = false,
        unique = true,
        length = 100
    )
    private String username;

    @Column(
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal cash;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    public User() {
    }


    public User(String username) {

        this.username = username;

        this.cash =
            new BigDecimal("10000.00");

        this.createdAt =
            LocalDateTime.now();
    }


    public UUID getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public BigDecimal getCash() {
        return cash;
    }


    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}