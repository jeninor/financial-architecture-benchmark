package com.juanesteban.tcc.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(
        ResourceNotFoundException.class
    )
    public ResponseEntity<?> notFound(
        ResourceNotFoundException ex
    ) {

        return ResponseEntity
            .status(
                HttpStatus.NOT_FOUND
            )
            .body(
                Map.of(

                    "timestamp",
                    LocalDateTime
                        .now()
                        .toString(),

                    "status",
                    404,

                    "error",
                    ex.getMessage()
                )
            );
    }


    @ExceptionHandler(
        IllegalArgumentException.class
    )
    public ResponseEntity<?> badRequest(
        IllegalArgumentException ex
    ) {

        return ResponseEntity
            .badRequest()
            .body(
                Map.of(

                    "timestamp",
                    LocalDateTime
                        .now()
                        .toString(),

                    "status",
                    400,

                    "error",
                    ex.getMessage()
                )
            );
    }


    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    public ResponseEntity<?> validation(
        MethodArgumentNotValidException ex
    ) {

        return ResponseEntity
            .badRequest()
            .body(
                Map.of(

                    "timestamp",
                    LocalDateTime
                        .now()
                        .toString(),

                    "status",
                    400,

                    "error",
                    "Invalid request"
                )
            );
    }
}