package com.example.model;

public record Fingerprint(byte[] token, FingerType fingerType) {
    public Fingerprint {
        if (token == null || token.length == 0) {
            throw new IllegalArgumentException("Token cannot be empty or empty.");
        }

        if (fingerType == null) {
            throw new IllegalArgumentException("FingerType cannot be null.");
        }
    }
}
