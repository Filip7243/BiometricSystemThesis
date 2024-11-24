package com.bio.bio_backend.dto;

public record AddRoomRequest(String roomNumber, Integer floor, Long buildingId, String deviceHardwareId) {
    public AddRoomRequest {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be null or empty.");
        }

        if (floor == null) {
            throw new IllegalArgumentException("Floor cannot be null.");
        }

        if (buildingId == null) {
            throw new IllegalArgumentException("BuildingId cannot be null.");
        }
    }
}
