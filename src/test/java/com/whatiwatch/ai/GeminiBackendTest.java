package com.whatiwatch.ai;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.whatiwatch.config.AiUnavailableException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;


class GeminiBackendTest {

    private MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    private GeminiBackend backendPointingAtMock() {
        String baseUrl = server.url("/").toString();
        return new GeminiBackend(baseUrl, "test-key", "gemini-2.5-flash");
    }

    @Test
    void parsesSuccessfulResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        {
                          "candidates": [
                            {
                              "content": {
                                "role": "model",
                                "parts": [ { "text": "You'd love Parasite." } ]
                              },
                              "finishReason": "STOP"
                            }
                          ],
                          "usageMetadata": {
                            "promptTokenCount": 10,
                            "candidatesTokenCount": 20,
                            "totalTokenCount": 30
                          }
                        }
                        """)
            .addHeader("Content-Type", "application/json"));
        
        AiResponse result = backendPointingAtMock().complete("recommend a movie");

        assertEquals("You'd love Parasite.", result.text());
        assertEquals("gemini-2.5-flash", result.model());
        assertEquals(30, result.tokensUsed());
    }

    @Test
    void sendsCorrectRequestToGemini() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        {
                          "candidates": [ { "content": { "parts": [ { "text": "ok" } ] } } ],
                          "usageMetadata": { "totalTokenCount": 5 }
                        }
                        """));
        
        backendPointingAtMock().complete("my prompt");

        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/v1beta/models/gemini-2.5-flash:generateContent", recorded.getPath());

        assertEquals("test-key", recorded.getHeader("x-goog-api-key"));

        String sentBody = recorded.getBody().readUtf8();
        assertTrue(sentBody.contains("\"role\":\"user\""));
        assertTrue(sentBody.contains("\"parts\""));
        assertTrue(sentBody.contains("my prompt"));
    }

    @Test
    void missingUsageDefaultsToZeroTokens() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        {
                          "candidates": [ { "content": { "parts": [ { "text": "some text" } ] } } ]
                        }
                        """));

        AiResponse result = backendPointingAtMock().complete("prompt");

        assertEquals(0, result.tokensUsed());
    }

    @Test
    void emptyCandidatesThrows() {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "candidates": [] }
                        """));

        assertThrows(AiUnavailableException.class,
            () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void emptyPartsThrows() {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "candidates": [ { "content": { "parts": "" } } ] }
                        """));
        
        assertThrows(AiUnavailableException.class,
            () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void emptyTextThrows() {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "candidates": [ { "content": { "parts": [ { "text": "" } ] } } ] }
                        """));
        
        assertThrows(AiUnavailableException.class,
            () -> backendPointingAtMock().complete("prompt"));
    }


    @Test
    void httpErrorThrows() {
        server.enqueue(new MockResponse().setResponseCode(400));

        assertThrows(AiUnavailableException.class, 
            () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void unreachableServerThrows() throws IOException {
        server.shutdown();

        assertThrows(AiUnavailableException.class, 
            () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void nullPromptThrows() {
        assertThrows(IllegalArgumentException.class, 
            () -> backendPointingAtMock().complete(null));
    }

}
