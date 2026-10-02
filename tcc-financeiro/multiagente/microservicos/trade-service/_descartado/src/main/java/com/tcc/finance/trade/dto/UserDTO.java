package com.tcc.finance.trade.dto;

import java.math.BigDecimal;

/** Espelha a resposta do user-service (GET/POST /users/**). */
public class UserDTO {
    private String username;
    private BigDecimal saldo;

    public UserDTO() {
    }

    public UserDTO(String username, BigDecimal saldo) {
        this.username = username;
        this.saldo = saldo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
