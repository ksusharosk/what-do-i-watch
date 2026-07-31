package com.whatiwatch.config;

// Throws when API limit is hit
public class ApiLimitException extends AppException {
    public ApiLimitException(String provider) {
        super("Free tier limit reached for " + provider + ". " + 
            "Wait for your quota to reset or switch to Ollama for unlimited local interference. "
        );
    }
}
