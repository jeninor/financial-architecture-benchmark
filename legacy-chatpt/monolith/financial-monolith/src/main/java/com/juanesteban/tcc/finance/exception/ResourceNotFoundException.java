package com.juanesteban.tcc.finance.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(
        String message
    ) {

        super(message);
    }
}