package com.bio.bio_backend.dto;

import com.bio.bio_backend.model.FingerType;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

public record EnrollmentRequest(MultipartFile file, FingerType type, String hardwareId) {  // hardwareId - id of scanner associated with room
    public EnrollmentRequest {
        if (file == null || file.isEmpty() || !Objects.equals(file.getContentType(), "image/bmp")) {
            throw new IllegalArgumentException("File is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Finger type is required");
        }
        if (hardwareId == null) {
            throw new IllegalArgumentException("Hardware id is required");
        }
    }
}
