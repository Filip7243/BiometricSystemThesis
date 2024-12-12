package com.bio.bio_backend.dto;

import com.bio.bio_backend.model.FingerType;

public record UserEnrollmentConfirmationDTO(String firstName,
                                            Boolean isConfirmed,
                                            FingerType fingerType,
                                            Long count) {
}
