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
 - AI backend backed by Google's Gemini API.
 - Unlike Groq, Gemini is not OpenAI-compatible
 - Requires an API key, can be free-tier
*/
public final class GeminiBackend implements AiBackend {

    private static final MediaType JSON = MediaType.get("application/json");
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com";

    private final OkHttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    /**
     * @param apiKey the Gemini api key
     * @param model the model name, e.g "gemini-2.5-flash"
     */
    public GeminiBackend(String apiKey, String model) {
        this(DEFAULT_BASE_URL, apiKey, model);
    }

    // Full constructor allowing custom base URL
    public GeminiBackend(String baseUrl, String apiKey, String model) {
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
            .url(baseUrl + "/v1beta/models/" + model + ":generateContent")
            .addHeader("x-goog-api-key", apiKey)
            .post(RequestBody.create(requestJson, JSON))
            .build();
        
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new AiUnavailableException("Gemini", 
                    new IOException("Unsuccessful response: HTTP " + response.code()));
            }

            String responseBody = response.body().string();
            return parseResponse(responseBody);
        } catch (IOException e) {
            throw new AiUnavailableException("Gemini", e);
        }
    }

    @Override
    public String name() {
        return "gemini";
    }

    // Builds Gemini's contents/parts request { "contents": [ { "role": "user", "parts": [ { "text": prompt } ] } ] }
    private String buildRequestBody(String prompt) {
        ObjectNode body = mapper.createObjectNode();

        ArrayNode contents = body.putArray("contents");
        ObjectNode content = contents.addObject();
        content.put("role", "user");

        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", prompt);

        return body.toString();
    }

    /*
     - Parses Gemini's response
     - Text lives at candidates[0].content.parts[0].text
     - Token totals at usageMetadata.totalTokenCount
    */
    private AiResponse parseResponse(String responseBody) throws AiUnavailableException {
        try {
            JsonNode root = mapper.readTree(responseBody);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AiUnavailableException("Gemini", 
                    new IOException("Response contained no candidates"));
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new AiUnavailableException("Gemini", 
                    new IOException("Response contained no parts"));
            }

            JsonNode textNode = parts.get(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new AiUnavailableException("Gemini", 
                    new IOException("Response contained no text"));
            }
                        
            int totalTokens = root.path("usageMetadata").path("totalTokenCount").asInt(0);

            return new AiResponse(textNode.asText(), model, totalTokens);

        } catch (IOException e) {
            throw new AiUnavailableException("Gemini", e);
        }
    }

}