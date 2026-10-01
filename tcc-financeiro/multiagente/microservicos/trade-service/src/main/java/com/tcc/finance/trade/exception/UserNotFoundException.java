package com.tcc.finance.trade.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Usuario nao encontrado: " + username);
    }
}
