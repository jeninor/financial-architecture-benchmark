package com.juanesteban.tcc.market.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
}