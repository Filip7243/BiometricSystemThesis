package com.bio.bio_backend.dto;

public record CreateRoomRequest(String roomNumber, Integer floor, String deviceHardwareId) {
    public CreateRoomRequest {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be null or empty.");
        }

        if (floor == null || floor < 0) {
            throw new IllegalArgumentException("Floor cannot be null.");
        }
    }
}
