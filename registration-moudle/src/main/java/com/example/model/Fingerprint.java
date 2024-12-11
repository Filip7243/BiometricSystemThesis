package com.example.model;

public record Fingerprint(byte[] token, FingerType fingerType, byte[] originalImage) {
    public Fingerprint {
        if (token == null || token.length == 0) {
            throw new IllegalArgumentException("Token cannot be empty or empty.");
        }

        if (fingerType == null) {
            throw new IllegalArgumentException("FingerType cannot be null.");
        }

        if (originalImage == null || originalImage.length == 0) {
            throw new IllegalArgumentException("OriginalImage cannot be empty or empty.");
        }
    }
}
