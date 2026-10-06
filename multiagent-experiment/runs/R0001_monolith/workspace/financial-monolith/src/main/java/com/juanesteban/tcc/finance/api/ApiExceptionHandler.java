package com.juanesteban.tcc.finance.api;

import com.juanesteban.tcc.finance.domain.BadRequestException;
import com.juanesteban.tcc.finance.domain.NotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({BadRequestException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> badRequest(Exception e) {
        return body(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(Exception e) {
        return body(HttpStatus.NOT_FOUND, e);
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, Exception e) {
        return ResponseEntity.status(status).body(Map.of("error", String.valueOf(e.getMessage())));
    }
}
