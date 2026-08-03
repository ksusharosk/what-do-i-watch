package com.whatiwatch.config;

import io.github.cdimascio.dotenv.Dotenv;

/*
- Central config class for everything.
- Creates and connects all the major components of the app.
- Oly one instance of this exists.
*/
public class AppConfig {

    private final Dotenv dotenv;

    public AppConfig() {
        this.dotenv = Dotenv.load();
        new ConfigValidator(dotenv).validate();
    }

    // returns the TMDB API key
    public String tmdbApiKey() {
        return dotenv.get("TMDB_API_KEY");
    }

    // returns the chosen AI backend name, defaults to ollama
    public String aiBackend() {
        return dotenv.get("AI_BACKEND", "ollama");
    }

    // returns the API key for the chosen AI backend, returns null for ollama
    public String aiApiKey() {
        return switch (aiBackend()) {
            case "groq" -> dotenv.get("GROQ_API_KEY");
            case "gemini" -> dotenv.get("GEMINI_API_KEY");
            default -> null;
        };
    }

    // returns the Ollama base URL, when AI_BACKEND == ollama
    public String ollamaBaseUrl() {
        return dotenv.get("OLLAMA_BASE_URL", "http://localhost:11434");
    }

    // returns maximum number of AI API requests allowed
    public int maxAiRequests() {
        String value = dotenv.get("GROQ_MAX_REQUESTS", "50");
        return Integer.parseInt(value);
    }

    // returns the preferred UI language, defaults to english
    public String defaultLanguage() {
        return dotenv.get("DEFAULT_LANGUAGE", "en");
    }
    
}
