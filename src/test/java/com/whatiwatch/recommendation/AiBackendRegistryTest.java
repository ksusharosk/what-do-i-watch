package com.whatiwatch.recommendation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.ai.AiResponse;
import com.whatiwatch.config.AiUnavailableException;

class AiBackendRegistryTest {

    /** A minimal fake backend that just reports a name. */
    private static AiBackend fakeBackend(String name) {
        return new AiBackend() {
            @Override
            public AiResponse complete(String prompt) {
                return new AiResponse("fake response", name, 0);
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    @Test
    void nullOrEmptyBackendListRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AiBackendRegistry(null));
        assertThrows(IllegalArgumentException.class,
                () -> new AiBackendRegistry(List.of()));
    }

    @Test
    void getsBackendByName() {
        AiBackend groq = fakeBackend("groq");
        AiBackend ollama = fakeBackend("ollama");
        AiBackendRegistry registry = new AiBackendRegistry(List.of(groq, ollama));

        assertEquals(groq, registry.get("groq"));
        assertEquals(ollama, registry.get("ollama"));
    }

    @Test
    void lookupIsCaseInsensitive() {
        AiBackend groq = fakeBackend("groq");
        AiBackendRegistry registry = new AiBackendRegistry(List.of(groq));

        assertEquals(groq, registry.get("Groq"));
        assertEquals(groq, registry.get("GROQ"));
    }

    @Test
    void unknownBackendThrows() {
        AiBackendRegistry registry = new AiBackendRegistry(List.of(fakeBackend("groq")));

        assertThrows(AiUnavailableException.class,
                () -> registry.get("gemini"));
    }

    @Test
    void nullNameRejected() {
        AiBackendRegistry registry = new AiBackendRegistry(List.of(fakeBackend("groq")));

        assertThrows(IllegalArgumentException.class,
                () -> registry.get(null));
    }

    @Test
    void hasReportsRegisteredBackends() {
        AiBackendRegistry registry = new AiBackendRegistry(List.of(fakeBackend("groq")));

        assertTrue(registry.has("groq"));
        assertTrue(registry.has("GROQ"));       // case-insensitive
        assertFalse(registry.has("gemini"));
        assertFalse(registry.has(null));
    }
}
