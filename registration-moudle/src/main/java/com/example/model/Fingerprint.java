package com.example.model;

public record Fingerprint(Long id, String token, FingerType fingerType, Long userId) {
    public Fingerprint {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be empty or blank.");
        }

        if (fingerType == null) {
            throw new IllegalArgumentException("FingerType cannot be null.");
        }

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
    }
}
