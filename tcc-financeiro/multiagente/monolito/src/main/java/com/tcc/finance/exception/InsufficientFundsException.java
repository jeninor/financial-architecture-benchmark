package com.tcc.finance.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Saldo insuficiente para concluir a compra");
    }
}
