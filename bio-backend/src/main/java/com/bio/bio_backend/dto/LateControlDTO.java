package com.bio.bio_backend.dto;

import java.sql.Timestamp;

public record LateControlDTO(String firstName,
                             String lastName,
                             String roomNumber,
                             String buildingNumber,
                             Timestamp enrollmentDate) {
}
