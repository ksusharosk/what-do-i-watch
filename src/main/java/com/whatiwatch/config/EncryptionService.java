package com.whatiwatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

/**
 * Encrypts and secrypts sensitive strings (users' AI API keys) for stprage at rest,
 * using Spring Security's AES-256-GCM encryptor.
 */
@Service
public class EncryptionService {

    private final TextEncryptor encryptor;

    public EncryptionService(@Value("${encryption.password}") String password,
                             @Value("${encryption.salt}") String salt) {
        if (password == null || password.isBlank() || salt == null || salt.isBlank()) {
            throw new IllegalStateException(
                "encryption.password and encryption.salt must be configured");
        }

        this.encryptor = Encryptors.text(password, salt);
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }
        return encryptor.encrypt(plaintext);
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return null;
        }
        return encryptor.decrypt(ciphertext);
    }
    
}
