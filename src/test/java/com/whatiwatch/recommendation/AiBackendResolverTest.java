package com.whatiwatch.recommendation;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.ai.AiResponse;
import com.whatiwatch.config.AiUnavailableException;
import com.whatiwatch.config.EncryptionService;
import com.whatiwatch.domain.user.User;
import com.whatiwatch.domain.user.UserPreferences;

class AiBackendResolverTest {

    private EncryptionService encryptionService;
    private AiBackend sharedGroq;
    private AiBackendRegistry registry;
    private AiBackendResolver resolver;

    @BeforeEach
    void setUp() {
        encryptionService = Mockito.mock(EncryptionService.class);

        // A stand-in shared backend named "groq" in the registry.
        sharedGroq = new AiBackend() {
            @Override public AiResponse complete(String prompt) { return new AiResponse("x", "groq", 0); }
            @Override public String name() { return "groq"; }
        };
        registry = new AiBackendRegistry(List.of(sharedGroq));

        resolver = new AiBackendResolver(registry, encryptionService,
                "openai/gpt-oss-120b", "gemini-2.5-flash");
    }

    /** Builds a user whose stored (encrypted) API key is the given value. */
    private User userWithKey(String encryptedKey) {
        UserPreferences prefs = new UserPreferences(
                List.of(), List.of(), List.of(), List.of(),
                "en", List.of(), List.of(), "groq", encryptedKey);
        User user = User.newUser("google1", "a@example.com", "Alice");
        return user.withPreferences(prefs);
    }

    @Test
    void guestGetsSharedBackend() throws Exception {
        AiBackend result = resolver.resolve(null, "groq");

        assertSame(sharedGroq, result);              // the shared instance
        verify(encryptionService, never()).decrypt(anyString());  // no decryption for a guest
    }

    @Test
    void userWithoutKeyGetsSharedBackend() throws Exception {
        User user = userWithKey(null);   // no stored key

        AiBackend result = resolver.resolve(user, "groq");

        assertSame(sharedGroq, result);
        verify(encryptionService, never()).decrypt(anyString());
    }

    @Test
    void userWithKeyGetsPersonalBackend() throws Exception {
        User user = userWithKey("ENCRYPTED_KEY");
        when(encryptionService.decrypt("ENCRYPTED_KEY")).thenReturn("gsk_realkey");

        AiBackend result = resolver.resolve(user, "groq");

        assertNotSame(sharedGroq, result);   // a NEW backend, not the shared one
        verify(encryptionService).decrypt("ENCRYPTED_KEY");   // their key was decrypted
    }

    @Test
    void ollamaAlwaysUsesSharedEvenWithUserKey() throws Exception {
        // A user could have a key set, but Ollama is local/keyless — should use shared.
        User user = userWithKey("ENCRYPTED_KEY");
        // Register a shared ollama backend for this test.
        AiBackend sharedOllama = new AiBackend() {
            @Override public AiResponse complete(String prompt) { return new AiResponse("x", "ollama", 0); }
            @Override public String name() { return "ollama"; }
        };
        AiBackendResolver r = new AiBackendResolver(
                new AiBackendRegistry(List.of(sharedOllama)),
                encryptionService, "m", "m");

        AiBackend result = r.resolve(user, "ollama");

        assertSame(sharedOllama, result);   // shared, not a personal build
    }

    @Test
    void unknownBackendThrows() {
        // Registry only has "groq"; asking for "gemini" (with no user key) should fail.
        assertThrows(AiUnavailableException.class,
                () -> resolver.resolve(null, "gemini"));
    }
}