package com.tcc.finance.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(long quantity) {
        super("Quantidade invalida: " + quantity);
    }
}
