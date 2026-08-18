package com.whatiwatch.recommendation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.whatiwatch.ai.AiBackend;
import com.whatiwatch.config.AiUnavailableException;


/*
 - Holds the available AI backends and selects one by name
 - A user's preferences store a backend name (e.g "groq")
 this maps that name to the matching AiBackend implementation.
*/
public final class AiBackendRegistry {

    private final Map<String, AiBackend> backends = new LinkedHashMap<>();

    /**
     * @param available the backends to register
     */
    public AiBackendRegistry(List<AiBackend> available) {
        if (available == null || available.isEmpty()) {
            throw new IllegalArgumentException("at least one backend is required");
        }
        for (AiBackend backend : available) {
            backends.put(backend.name(), backend);
        }
    }

    /**
     * Returns the backend registered under the given name
     * 
     * @param name the backend name, e.g "groq" (case-insensitive)
     * @throws AiUnavailableException if not backend is registered under this name
     */
    public AiBackend get(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("backend name cannot be null");
        }
        AiBackend backend = backends.get(name.toLowerCase());
        if (backend == null) {
            throw new AiUnavailableException(name, 
                new IllegalArgumentException("No AI backend registered under this name")
            );
        }
        return backend;
    }

    // Returns true if a backend is registered under the given name
    public boolean has(String name) {
        return name != null && backends.containsKey(name.toLowerCase());
    }
    
}
