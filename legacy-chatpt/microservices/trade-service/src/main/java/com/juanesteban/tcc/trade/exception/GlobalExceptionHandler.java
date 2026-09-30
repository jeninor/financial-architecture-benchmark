package com.juanesteban.tcc.trade.exception;

import feign.FeignException;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
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


    @ExceptionHandler(
        FeignException.class
    )
    public ResponseEntity<String> feign(
        FeignException ex
    ) {

        int status =
            ex.status() > 0
                ? ex.status()
                : 503;


        String body =
            ex.contentUTF8();


        if (
            body == null ||
            body.isBlank()
        ) {

            body =
                """
                {
                  "status":503,
                  "error":"Remote service unavailable"
                }
                """;
        }


        return ResponseEntity
            .status(status)
            .contentType(
                MediaType.APPLICATION_JSON
            )
            .body(body);
    }
}