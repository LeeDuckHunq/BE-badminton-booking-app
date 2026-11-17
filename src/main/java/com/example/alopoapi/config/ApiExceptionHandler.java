package com.example.alopoapi.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handle(ResponseStatusException exception) {
        String message = exception.getReason() == null ? "Yêu cầu không hợp lệ" : exception.getReason();
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("status", exception.getStatusCode().value(), "message", message));
    }
}
