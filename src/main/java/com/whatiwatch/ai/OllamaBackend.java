package com.whatiwatch.ai;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.whatiwatch.config.AiUnavailableException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 - AI Backend backed by a local Ollama server (http://localhost:11434)
 - Runs on the user's machine
 - Uses Ollama's /api/generate endpoint with streaming disabled, so each call returns a single JSON object.
*/
public final class OllamaBackend implements AiBackend {
    private static final MediaType JSON = MediaType.get("application/json");

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String model;

    /**
        @param baseUrl the Ollama server URL, e.g. "http://localhost:11434"
        @param model the model name to use, e.g. "llama3.1"
    */
    public OllamaBackend(String baseUrl, String model) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl cannot be null or blank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model cannot be null or blank");
        }

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.http = new OkHttpClient().newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .readTimeout(java.time.Duration.ofMinutes(5))
            .writeTimeout(java.time.Duration.ofSeconds(30))
            .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(String prompt) throws AiUnavailableException {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt cannot be null or blank");
        }
        String requestJson = buildRequestBody(prompt);
        Request request = new Request.Builder()
            .url(baseUrl + "/api/generate")
            .post(RequestBody.create(requestJson, JSON))
            .build();
        
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new AiUnavailableException("Ollama", 
                    new IOException("Unsuccessful response: HTTP " + response.code()));   
            }

            String responseBody = response.body().string();
            return parseResponse(responseBody);

        } catch (IOException e) {
            // Thrown when the server is unreachable
            throw new AiUnavailableException("Ollama", e);
        }
    }

    @Override
    public String name() {
        return "ollama";
    }

    // Builds the JSON request body
    private String buildRequestBody(String prompt) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("stream", false);
        return body.toString();
    }

    /*
     - Parses Ollama's /api/generate response into an AiResponse
     - Sums prompt + completion token counts
    */
    private AiResponse parseResponse(String responseBody) throws AiUnavailableException {
        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode textNode = root.get("response");

            if (textNode == null || textNode.asText().isBlank()) {
                throw new AiUnavailableException("Ollama",
                    new IOException("Response contained no text"));
            }

            int promptTokens = root.path("prompt_eval_count").asInt(0);
            int completionTokens = root.path("eval_count").asInt(0);

            return new AiResponse(textNode.asText(), model, promptTokens + completionTokens);

        } catch (IOException e) {
            throw new AiUnavailableException("Ollama", e);
        }
    }
    
}
