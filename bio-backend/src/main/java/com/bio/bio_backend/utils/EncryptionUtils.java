package com.bio.bio_backend.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Klasa dostarczająca metody szyfrowania i deszyfrowania z wykorzystaniem algorytmu AES.
 * <p>
 * Implementacja wykorzystuje algorytm AES w trybie CBC z wypełnieniem PKCS5.
 * Narzędzie łączy wektor inicjalizacyjny (IV) i zaszyfrowane dane w jedną tablicę bajtów
 * do przechowywania lub przesyłania, a następnie rozdziela je podczas deszyfrowania.
 */
public class EncryptionUtils {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding"; // Algorytm AES w trybie CBC z wypełnieniem PKCS5.
    private static final int IV_LENGTH = 16; // Długość wektora IV (16 bajtów dla AES).

    private static final String SECRET_KEY = "YourSecretKey123"; // Klucz szyfrujący

    /**
     * Szyfruje dane za pomocą algorytmu AES.
     *
     * @param data Tablica bajtów zawierająca dane do zaszyfrowania.
     * @return Tablica bajtów zawierająca zaszyfrowane dane z dołączonym wektorem IV.
     * @throws Exception W przypadku problemów z szyfrowaniem.
     */
    public static byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec secretKey = generateKey(SECRET_KEY);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Generowanie losowego wektora IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);

        // Łączenie IV z zaszyfrowanymi danymi
        byte[] combined = new byte[IV_LENGTH + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encryptedData, 0, combined, IV_LENGTH, encryptedData.length);

        return combined;
    }

    /**
     * Odszyfrowuje dane za pomocą algorytmu AES.
     *
     * @param combinedData Tablica bajtów zawierająca wektor IV i zaszyfrowane dane.
     * @return Tablica bajtów zawierająca odszyfrowane dane.
     * @throws Exception W przypadku problemów z deszyfrowaniem.
     */
    public static byte[] decrypt(byte[] combinedData) throws Exception {
        System.out.println("Received data length: " + combinedData.length);
        System.out.println("First 16 bytes (IV): " + Arrays.toString(Arrays.copyOf(combinedData, IV_LENGTH)));

        SecretKeySpec secretKey = generateKey(SECRET_KEY);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        // Wyodrębnianie wektora IV z zaszyfrowanych danych
        byte[] iv = new byte[IV_LENGTH];
        byte[] encryptedData = new byte[combinedData.length - IV_LENGTH];
        System.arraycopy(combinedData, 0, iv, 0, IV_LENGTH);
        System.arraycopy(combinedData, IV_LENGTH, encryptedData, 0, encryptedData.length);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        return cipher.doFinal(encryptedData);
    }

    /**
     * Generuje klucz szyfrowania na podstawie podanego ciągu znaków.
     *
     * @param secret Ciąg znaków używany do wygenerowania klucza.
     * @return Klucz szyfrowania w postaci obiektu {@link SecretKeySpec}.
     * @throws Exception W przypadku problemów z generowaniem klucza.
     */
    private static SecretKeySpec generateKey(String secret) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] key = digest.digest(bytes);

        // SHA tworzy klucz o długości 256 bitów (32 bajty),
        // a algorytm AES jest używany w wersji 128 (16 bajtów)
        // dlatego bierzemy tylko pierwszych 16 bitów
        return new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
    }
}
