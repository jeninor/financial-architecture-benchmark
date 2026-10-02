package com.tcc.finance.trade.exception;

public class InsufficientSharesException extends RuntimeException {
    public InsufficientSharesException() {
        super("Quantidade de acoes insuficiente para a venda");
    }
}
