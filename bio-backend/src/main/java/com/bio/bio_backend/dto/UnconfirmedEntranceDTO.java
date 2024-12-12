package com.bio.bio_backend.dto;

public record UnconfirmedEntranceDTO(String firstName,
                                     String lastName,
                                     String roomNumber,
                                     String buildingNumber,
                                     Long count) {
}
