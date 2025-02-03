package com.example.client.dto;

public record FingerprintDTO(Long id,
                             byte[] token,
                             String fingerType,
                             Long userId,
                             byte[] originalImage) {

    public FingerprintDTO {
        if (token == null || token.length == 0) {
            throw new IllegalArgumentException("Token cannot be empty or empty.");
        }

        if (fingerType == null || fingerType.isBlank()) {
            throw new IllegalArgumentException("FingerType cannot be null or empty.");
        }

        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null.");
        }
    }
}
