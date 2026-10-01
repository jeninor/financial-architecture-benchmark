package com.tcc.finance.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username ja cadastrado: " + username);
    }
}
