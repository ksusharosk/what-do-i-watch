package com.whatiwatch.recommendation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.ai.GeminiBackend;
import com.whatiwatch.ai.GroqBackend;
import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.config.EncryptionService;
import com.whatiwatch.domain.user.User;

/**
 * Picks the AiBackend to use for a request. If the logged-in user supplied
 * their own API key, a backend is built with that (decrypted) key. Otherwise, 
 * the shared app-wide backend is used 
 */
@Component
public class AiBackendResolver {

    private final AiBackendRegistry sharedBackends;
    private final EncryptionService encryptionService;
    private final String groqModel;
    private final String geminiModel;

    public AiBackendResolver(AiBackendRegistry sharedBackends,
                            EncryptionService encryptionService,
                            @Value("${ai.groq.model:openai/gpt-oss-120b}") String groqModel,
                            @Value("${ai.gemini.model:gemini-2.5-flash}") String geminiModel) {
        this.sharedBackends = sharedBackends;
        this.encryptionService = encryptionService;
        this.groqModel = groqModel;
        this.geminiModel = geminiModel;
    }

    /**
     * Resolves the backend for the given user and backend name
     * 
     * @param user the logged-in user, or null for a guest
     * @param backendName the requested backend
     */
    public AiBackend resolve(User user, String backendName) throws AiUnavailableException {
        // Try to use the user's own key, if they have one for this backend
        if (user != null) {
            AiBackend personal = buildPersonalBackend(user, backendName);
            if (personal != null) {
                return personal;
            }
        }
        // Fall back to the shared app-wide backend
        return sharedBackends.get(backendName);
    }

    /**
     * Builds a backend using the user's own decrypted key, or null if they
     * have no key (or backend is Ollama)
     */
    private AiBackend buildPersonalBackend(User user, String backendName) {
        String encryptedKey = user.preferences().encryptedApiKey();
        if (encryptedKey == null || encryptedKey.isBlank()) {
            return null;  // no personal key 
        }

        String key = encryptionService.decrypt(encryptedKey);
        if (key == null || key.isBlank()) {
            return null;
        }

        return switch (backendName.toLowerCase()) {
            case "groq" -> new GroqBackend(key, groqModel);
            case "gemini" -> new GeminiBackend(key, geminiModel);
            default -> null; // ollama or unknown -> use shared
        };
    }
    
}
