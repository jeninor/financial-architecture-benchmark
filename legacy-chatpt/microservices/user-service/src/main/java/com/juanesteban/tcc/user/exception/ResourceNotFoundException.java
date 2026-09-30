package com.juanesteban.tcc.user.exception;

public class ResourceNotFoundException
        extends RuntimeException {

    public ResourceNotFoundException(
        String message
    ) {

        super(message);
    }
}