package com.whatiwatch.config;

//Thrown when the AI backend cannot be reached or returns an error
public class AiUnavailableException extends AppException {
    public AiUnavailableException(String backend, Throwable cause) {
        super("AI backend '" + backend + "' is unavailable. " +
            "Check your API key and internet connection.", cause);
    }
    
}
