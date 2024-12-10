package com.bio.bio_backend.dto;

import java.util.List;

public record CreateBuildingRequest(String buildingNumber, String street, List<CreateRoomRequest> rooms) {
    public CreateBuildingRequest {
        if (buildingNumber == null || buildingNumber.isBlank()) {
            throw new IllegalArgumentException("Building number cannot be null or blank.");
        }

        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank.");
        }

        if (rooms == null) {
            throw new IllegalArgumentException("Rooms cannot be null or empty.");
        }
    }
}
