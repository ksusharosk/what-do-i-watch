package com.whatiwatch.ai;

/*
 - The result of an AI completion — the model's text plus useful metadata.
 - @param text       the model's actual text response
 - @param model      the model that produced it, e.g. "llama-3.3-70b"
 - @param tokensUsed total tokens consumed by this call (0 if the backend doesn't report it)
 */

public record AiResponse( String text, String model, int tokensUsed) {

    public AiResponse {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("AI response text can't be null or blank.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("AI response model cannot be null or blank");
        } 
        if (tokensUsed < 0) {
            throw new IllegalArgumentException("tokensUsed cannot be negative");
        }
    }
    
}
