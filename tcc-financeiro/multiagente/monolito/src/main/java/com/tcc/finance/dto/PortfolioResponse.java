package com.tcc.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {
    private String username;
    private BigDecimal saldo;
    private List<PosicaoResponse> posicoes;

    public PortfolioResponse(String username, BigDecimal saldo, List<PosicaoResponse> posicoes) {
        this.username = username;
        this.saldo = saldo;
        this.posicoes = posicoes;
    }

    public String getUsername() {
        return username;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public List<PosicaoResponse> getPosicoes() {
        return posicoes;
    }
}
