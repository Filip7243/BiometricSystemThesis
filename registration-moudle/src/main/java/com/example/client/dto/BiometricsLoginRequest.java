package com.example.client.dto;

import com.example.model.FingerType;

/*
 * Request wysyłany przy logowaniu do panelu administracyjnego przy użyciu danych biometrycznych
 */
public record BiometricsLoginRequest(byte[] file, FingerType type) {
    public BiometricsLoginRequest {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
    }
}
