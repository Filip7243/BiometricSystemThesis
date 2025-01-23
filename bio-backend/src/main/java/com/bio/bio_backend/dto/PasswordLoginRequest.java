package com.bio.bio_backend.dto;

public record PasswordLoginRequest(byte[] encryptedPassword) {
    public PasswordLoginRequest {
        if (encryptedPassword == null || encryptedPassword.length == 0) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
    }
}
