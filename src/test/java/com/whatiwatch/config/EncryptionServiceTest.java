package com.whatiwatch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncryptionServiceTest {

    private EncryptionService service;

    @BeforeEach
    void setUp() {
        // A real (test) password and hex salt — real crypto, not mocked.
        service = new EncryptionService(
                "test-password-1234",
                "0123456789abcdef0123456789abcdef");
    }

    @Test
    void encryptThenDecryptReturnsOriginal() {
        String original = "gsk_myRealGroqApiKey12345";

        String encrypted = service.encrypt(original);
        String decrypted = service.decrypt(encrypted);

        assertEquals(original, decrypted);   // round-trip works
    }

    @Test
    void encryptedTextDiffersFromPlaintext() {
        String original = "gsk_secret";

        String encrypted = service.encrypt(original);

        assertNotEquals(original, encrypted);   // it's actually scrambled
    }

    @Test
    void sameInputEncryptsDifferentlyEachTime() {
        // AES-GCM uses a random IV per encryption, so ciphertext differs.
        String original = "gsk_secret";

        String first = service.encrypt(original);
        String second = service.encrypt(original);

        assertNotEquals(first, second);   // different ciphertext...
        assertEquals(original, service.decrypt(first));   // ...but both decrypt correctly
        assertEquals(original, service.decrypt(second));
    }

    @Test
    void nullAndBlankAreHandled() {
        assertNull(service.encrypt(null));
        assertNull(service.encrypt(""));
        assertNull(service.decrypt(null));
        assertNull(service.decrypt(""));
    }

    @Test
    void missingConfigFailsFast() {
        assertThrows(IllegalStateException.class,
                () -> new EncryptionService("", "somesalt"));
        assertThrows(IllegalStateException.class,
                () -> new EncryptionService("password", ""));
    }
}