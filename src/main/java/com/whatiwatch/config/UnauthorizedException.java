package com.whatiwatch.config;

/** Thrown when an operation requires a logged-in user but none is present. */
public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(message);
    }
}