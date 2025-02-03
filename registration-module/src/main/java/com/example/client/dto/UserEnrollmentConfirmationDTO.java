package com.example.client.dto;

import com.example.model.FingerType;

public record UserEnrollmentConfirmationDTO(String firstName,
                                            Boolean isConfirmed,
                                            FingerType fingerType,
                                            Long count) {
}
