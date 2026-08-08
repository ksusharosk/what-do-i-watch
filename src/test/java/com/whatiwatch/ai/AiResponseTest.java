package com.whatiwatch.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class AiResponseTest {
    
    @Test
    void validResponseIsCreated() {
        AiResponse response = new AiResponse("You'd love Parasite.", "llama-3.3-70b",340);
        
        assertEquals("You'd love Parasite.", response.text());
        assertEquals("llama-3.3-70b", response.model());
        assertEquals(340, response.tokensUsed());
    }

    @Test
    void zeroTokensIsAllowed() {
        //Ollama doesn't report tokens, so 0 must be valid
        AiResponse response = new AiResponse("some text", "llama3", 0);

        assertEquals(0, response.tokensUsed());
    }

    @Test
    void nullTextIsRejected() {
        assertThrows(IllegalArgumentException.class, 
            () -> new AiResponse(null, "llama3", 10));
    }
    @Test
    void nullModelIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AiResponse("some text", null, 10));
    }

    @Test
    void blankModelIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AiResponse("some text", "  ", 10));
    }

    @Test
    void negativeTokensIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new AiResponse("some text", "llama3", -1));
    }

}
