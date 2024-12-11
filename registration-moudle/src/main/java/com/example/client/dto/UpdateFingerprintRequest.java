package com.example.client.dto;

public record UpdateFingerprintRequest(Long id,
                                       byte[] token,
                                       byte[] originalImage) {
    public UpdateFingerprintRequest {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        if (token == null || token.length == 0) {
            throw new IllegalArgumentException("Token cannot be empty or empty.");
        }

        if (originalImage == null || originalImage.length == 0) {
            throw new IllegalArgumentException("OriginalImage cannot be empty or empty.");
        }
    }
}
