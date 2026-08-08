package com.whatiwatch.ai;

import com.whatiwatch.config.AiUnavailableException;

/*
 - Contract for all AI backends (Ollama, Groq, Gemini)
 - Each backend is configured with its model at construction, then takes as 
 fully-built prompt and returns the model's response with metadata.
*/
public interface AiBackend {
    /**
     - Sends a prompt to the AI model and returns its response.
     - @param prompt the fully-built prompt (produced by PromptBuilder)
     - @return the model's response, wrapped with metadata
     - @throws AiUnavailableException if the backend cannot be reached or fails
     */

     AiResponse complete(String prompt) throws AiUnavailableException;

     // An identifier for this backend e.g "ollama", "groq", "gemini"
     String name(); 

}
