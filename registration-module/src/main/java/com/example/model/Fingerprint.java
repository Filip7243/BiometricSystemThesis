package com.example.model;

public record Fingerprint(FingerType fingerType, byte[] originalImage) {
    public Fingerprint {
        if (fingerType == null) {
            throw new IllegalArgumentException("FingerType cannot be null.");
        }

        if (originalImage == null || originalImage.length == 0) {
            throw new IllegalArgumentException("OriginalImage cannot be empty or empty.");
        }
    }
}
