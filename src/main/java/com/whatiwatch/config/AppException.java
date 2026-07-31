package com.whatiwatch.config;

/*
- Base exception for all app errors
- All other exceptione in the app extend this one
*/
public class AppException extends RuntimeException{
    public AppException(String message) {
        super(message);
    }
    
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
