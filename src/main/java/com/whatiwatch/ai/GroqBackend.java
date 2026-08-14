package com.whatiwatch.ai;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.whatiwatch.config.AiUnavailableException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/*
 - AI backend backed by Groq's cloud API (OpenAI-compatible)
 - Requires an API key, can be free-tier
*/
public final class GroqBackend implements AiBackend {

    private static final MediaType JSON = MediaType.get("application/json");
    private static final String DEFAULT_BASE_URL = "https://api.groq.com";

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    /**
     * @param apiKey the Groq api key
     * @param model the model name, e.g "openai/gpt-oss-120b"
     */
    public GroqBackend(String apiKey, String model) {
        this(DEFAULT_BASE_URL, apiKey, model);
    }

    // Full constructor allowing custom base URL
    public GroqBackend(String baseUrl, String apiKey, String model) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl cannot be null or blank");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey cannot be null or blank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model cannot be null or blank");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.http = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(String prompt) throws AiUnavailableException {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt cannot be null or blank");
        }

        String requestJson = buildRequestBody(prompt);
        Request request = new Request.Builder()
            .url(baseUrl + "/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer " + apiKey)
            .post(RequestBody.create(requestJson, JSON))
            .build();
        
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new AiUnavailableException("Groq", 
                    new IOException("Unsuccessful response: HTTP " + response.code()));
            }

            String responseBody = response.body().string();
            return parseResponse(responseBody);
        } catch (IOException e) {
            throw new AiUnavailableException("Groq", e);
        }
    }

    @Override
    public String name() {
        return "groq";
    }

    // Builds OpenAI compatible chat request. Prompt becomes a single "user" message inside a messages array
    private String buildRequestBody(String prompt) {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);

        ArrayNode messages = body.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        return body.toString();
    }

    /*
     - Parses the OpenAi-compatible response.
     - Text lives at choices[0].message.content
     - Token totals at usage.total_tokens
    */
    private AiResponse parseResponse(String responseBody) throws AiUnavailableException {
        try {
            JsonNode root = mapper.readTree(responseBody);

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new AiUnavailableException("Groq", 
                    new IOException("Response contained no choices"));
            }

            JsonNode contentNode = choices.get(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new AiUnavailableException("Groq", 
                    new IOException("Response contained no text"));
            }
            
            int totalTokens = root.path("usage").path("total_tokens").asInt(0);

            return new AiResponse(contentNode.asText(), model, totalTokens);

        } catch (IOException e) {
            throw new AiUnavailableException("Groq", e);
        }
    }

}
