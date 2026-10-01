package com.tcc.finance.user.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Saldo insuficiente");
    }
}
