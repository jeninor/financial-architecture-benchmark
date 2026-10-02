package com.tcc.finance.market.exception;

public class SymbolNotFoundException extends RuntimeException {
    public SymbolNotFoundException(String symbol) {
        super("Simbolo invalido: " + symbol);
    }
}
