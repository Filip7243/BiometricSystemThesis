package com.bio.bio_backend.dto;

import com.bio.bio_backend.model.FingerType;

public record FingerCreationRequest(byte[] token, FingerType fingerType, Long userId, byte[] originalImage) {
}
