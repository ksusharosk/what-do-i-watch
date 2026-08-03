package com.whatiwatch.config;

import java.util.ArrayList;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;

// Validates that all required configurations are present at startup
public class ConfigValidator {

    private final Dotenv dotenv;

    public ConfigValidator(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    /*
    - Validates all required config values
    - Throws IllegalStateException if anything is missing
    */
    public void validate() {
        List<String> missing = new ArrayList<>();

        //Always required
        requireKey("TMDB_API_KEY", missing);

        // Only required depenfing on chosen AI backend
        String backend = dotenv.get("AI_BACKEND", "ollama");
        switch (backend) {
            case "groq" -> requireKey("GROQ_API_KEY", missing);
            case "gemini" -> requireKey("GEMINI_API_KEY", missing);
            case "ollama" -> requireKey("OLLAMA_BASE_URL", missing);
            default -> throw new IllegalStateException(
                "Unknown AI backend: '" + backend + "'. " +
                "Valid options are: ollama, groq, gemini."                
            );
        }

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                "Missing required configuration: " + missing + "\n" +
                "Copy .env.example to .env and fill in your values."                
            );
        }
    }

    private void requireKey(String key, List<String> missing) {
        String value = dotenv.get(key, "");
        if (value.isBlank()) {
            missing.add(key);
        }
    }

}
