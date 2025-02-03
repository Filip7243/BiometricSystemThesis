package com.example.client.dto;

public record UpdateFingerprintRequest(Long id,
                                       byte[] encryptedImage) {
    public UpdateFingerprintRequest {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        if (encryptedImage == null || encryptedImage.length == 0) {
            throw new IllegalArgumentException("OriginalImage cannot be empty or empty.");
        }
    }
}
