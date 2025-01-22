package com.example.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionUtils {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private static final String SECRET_KEY = "YourSecretKey123";

    public static byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec secretKey = generateKey(SECRET_KEY);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);

        byte[] combined = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

        return combined;
    }

    public static byte[] decrypt(byte[] combinedData) throws Exception {
        SecretKeySpec secretKey = generateKey(SECRET_KEY);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        byte[] iv = new byte[IV_LENGTH];
        byte[] encryptedData = new byte[combinedData.length - IV_LENGTH];
        System.arraycopy(combinedData, 0, iv, 0, IV_LENGTH);
        System.arraycopy(combinedData, IV_LENGTH, encryptedData, 0, encryptedData.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        return cipher.doFinal(encryptedData);
    }

    private static SecretKeySpec generateKey(String secret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] key = digest.digest(bytes);
        return new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
    }
}
