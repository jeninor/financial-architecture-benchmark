package com.tcc.finance.exception;

public class SymbolNotFoundException extends RuntimeException {
    public SymbolNotFoundException(String symbol) {
        super("Simbolo invalido: " + symbol);
    }
}
