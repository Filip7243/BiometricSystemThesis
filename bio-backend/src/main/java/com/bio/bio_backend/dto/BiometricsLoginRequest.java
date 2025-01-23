package com.bio.bio_backend.dto;

import com.bio.bio_backend.model.FingerType;
import org.springframework.web.multipart.MultipartFile;

/*
 * Request wysyłany przy logowaniu do panelu administracyjnego przy użyciu danych biometrycznych
 */
public record BiometricsLoginRequest(byte[] file, FingerType type) {
    public BiometricsLoginRequest {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
    }
}
