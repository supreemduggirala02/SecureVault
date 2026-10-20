package com.securevault.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    @Value("${securevault.encryption.key}")
    private String secretKey;

    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plainText) {

        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            SecretKeySpec key = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(128, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            byte[] combined =
                    new byte[iv.length + encrypted.length];

            System.arraycopy(
                    iv, 0,
                    combined, 0,
                    iv.length
            );

            System.arraycopy(
                    encrypted, 0,
                    combined, iv.length,
                    encrypted.length
            );

            return Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {

        try {
            byte[] combined =
                    Base64.getDecoder()
                            .decode(encryptedText);

            byte[] iv = new byte[12];

            byte[] encrypted =
                    new byte[combined.length - 12];

            System.arraycopy(
                    combined, 0,
                    iv, 0,
                    12
            );

            System.arraycopy(
                    combined, 12,
                    encrypted, 0,
                    encrypted.length
            );

            SecretKeySpec key = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(128, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}