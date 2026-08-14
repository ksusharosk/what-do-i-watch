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


class GroqBackendTest {

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

    private GroqBackend backendPointingAtMock() {
        String baseUrl = server.url("/").toString();
        return new GroqBackend(baseUrl, "test-key", "openai/gpt-oss-120b");
    }

    @Test
    void parsesSuccessfulResponse() throws Exception {
        server.enqueue(new MockResponse()
            .setBody("""
                    {
                      "model": "openai/gpt-oss-120b",
                      "choices": [
                        {
                          "message": { "role": "assistant", "content": "You'd love Parasite." },
                          "finish_reason": "stop"                          
                        }
                    ],
                  "usage": { "prompt_tokens": 12, "completion_tokens": 8, "total_tokens": 20 }
                }
                """)
            .addHeader("Content-Type", "application/json"));
        
        AiResponse result = backendPointingAtMock().complete("recommend a movie");

        assertEquals("You'd love Parasite.", result.text());
        assertEquals("openai/gpt-oss-120b", result.model());
        assertEquals(20, result.tokensUsed());
    }

    @Test
    void sendsCorrectRequestToGroq() throws Exception {
        server.enqueue(new MockResponse()
            .setBody("""
                    {
                        "choices": [ { "message": { "content": "ok" } } ],
                        "usage": { "total_tokens": 5 } 
                    }
                    """));
        
        backendPointingAtMock().complete("my prompt");

        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/openai/v1/chat/completions", recorded.getPath());

        assertEquals("Bearer test-key", recorded.getHeader("Authorization"));

        String sentBody = recorded.getBody().readUtf8();
        assertTrue(sentBody.contains("\"model\":\"openai/gpt-oss-120b\""));
        assertTrue(sentBody.contains("\"role\":\"user\""));
        assertTrue(sentBody.contains("my prompt"));
    }

    @Test
    void missingUsageDefaultsToZeroTokens() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "choices": [ { "message": { "content": "some text" } } ] }
                        """));

        AiResponse result = backendPointingAtMock().complete("prompt");

        assertEquals(0, result.tokensUsed());
    }

    @Test
    void emptyChoicesThrows() {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "choices": [] }
                        """));

        assertThrows(AiUnavailableException.class,
            () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void emptyContentThrows() {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "choices": [ { "message": { "content": "" } } ] }
                        """));
        
        assertThrows(AiUnavailableException.class,
            () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void httpErrorThrows() {
        server.enqueue(new MockResponse().setResponseCode(401));

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
