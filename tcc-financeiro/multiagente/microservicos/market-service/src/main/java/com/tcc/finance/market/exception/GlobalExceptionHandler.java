package com.tcc.finance.market.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SymbolNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(SymbolNotFoundException ex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", HttpStatus.NOT_FOUND.value());
        payload.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(payload);
    }
}
