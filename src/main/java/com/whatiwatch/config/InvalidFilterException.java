package com.whatiwatch.config;

// Thrown when a user provides an invalid filter value
public class InvalidFilterException extends AppException{
    public InvalidFilterException(String message) {
        super(message);
    }
    
}
