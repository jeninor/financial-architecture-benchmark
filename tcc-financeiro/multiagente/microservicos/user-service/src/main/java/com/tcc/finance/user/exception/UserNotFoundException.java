package com.tcc.finance.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Usuario nao encontrado: " + username);
    }
}
