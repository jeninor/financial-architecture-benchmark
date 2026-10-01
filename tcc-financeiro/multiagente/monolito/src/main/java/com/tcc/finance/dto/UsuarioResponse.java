package com.tcc.finance.dto;

import java.math.BigDecimal;

public class UsuarioResponse {
    private String username;
    private BigDecimal saldo;

    public UsuarioResponse(String username, BigDecimal saldo) {
        this.username = username;
        this.saldo = saldo;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
