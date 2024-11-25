package com.example.client.dto;

public record UpdateFingerprintRequest(Long id, byte[] token) {
    public UpdateFingerprintRequest {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        if (token == null || token.length == 0) {
            throw new IllegalArgumentException("Token cannot be empty or empty.");
        }
    }
}
