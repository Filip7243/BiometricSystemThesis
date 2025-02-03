package com.example.client.dto;

import java.util.List;

public record BuildingDTO(Long id, String buildingNumber, String street, List<RoomDTO> rooms) {
    public BuildingDTO {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (buildingNumber == null || buildingNumber.isBlank()) {
            throw new IllegalArgumentException("Building number cannot be null or blank.");
        }

        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank.");
        }
    }

    @Override
    public String toString() {
        return "Building ID: %d, Building Number: %s, Street: %s"
                .formatted(id, buildingNumber, street);
    }
}
