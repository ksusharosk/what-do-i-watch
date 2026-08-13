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

class OllamaBackendTest {

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

    /** Points the backend at the mock server instead of a real Ollama. */
    private OllamaBackend backendPointingAtMock() {
        String baseUrl = server.url("/").toString();  // e.g. http://localhost:<random-port>/
        return new OllamaBackend(baseUrl, "llama3.1");
    }

    @Test
    void parsesSuccessfulResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        {
                          "model": "llama3.1",
                          "response": "You'd love Parasite.",
                          "done": true,
                          "prompt_eval_count": 12,
                          "eval_count": 8
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        AiResponse result = backendPointingAtMock().complete("recommend a movie");

        assertEquals("You'd love Parasite.", result.text());
        assertEquals("llama3.1", result.model());
        assertEquals(20, result.tokensUsed());  // 12 + 8
    }

    @Test
    void sendsCorrectRequestToOllama() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "response": "ok", "prompt_eval_count": 1, "eval_count": 1 }
                        """));

        backendPointingAtMock().complete("my prompt");

        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/api/generate", recorded.getPath());

        String sentBody = recorded.getBody().readUtf8();
        assertTrue(sentBody.contains("\"model\":\"llama3.1\""));
        assertTrue(sentBody.contains("\"stream\":false"));
        assertTrue(sentBody.contains("my prompt"));
    }

    @Test
    void missingTokenCountsDefaultToZero() throws Exception {
        // A model that doesn't report token counts
        server.enqueue(new MockResponse()
                .setBody("""
                        { "response": "some text" }
                        """));

        AiResponse result = backendPointingAtMock().complete("prompt");

        assertEquals(0, result.tokensUsed());
    }

    @Test
    void emptyResponseTextThrows() {
        server.enqueue(new MockResponse()
                .setBody("""
                        { "response": "", "prompt_eval_count": 1, "eval_count": 1 }
                        """));

        assertThrows(AiUnavailableException.class,
                () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void httpErrorThrows() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(AiUnavailableException.class,
                () -> backendPointingAtMock().complete("prompt"));
    }

    @Test
    void unreachableServerThrows() throws IOException {
        // Shut the server down so nothing is listening — simulates Ollama not running
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