package com.whatiwatch.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.config.ApiLimitException;
import com.whatiwatch.config.InvalidFilterException;
import com.whatiwatch.config.MovieNotFoundException;

/**
 * Translates application exceptions into clean JSON error responses,
 * instead of leaking stack traces
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler(AiUnavailableException.class)
        public ResponseEntity<Map<String, String>> handleAiUnavailable(AiUnavailableException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }
    
    @ExceptionHandler(ApiLimitException.class)
    public ResponseEntity<Map<String, String>> handleApiLimit(ApiLimitException e) {
        return error(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFilter(InvalidFilterException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMovieNotFound(MovieNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}

