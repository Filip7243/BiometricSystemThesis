package com.example.client.dto;

public record CreateRoomRequest(String roomNumber, Integer floor, String deviceHardwareId) {
    public CreateRoomRequest {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be null or empty.");
        }

        if (floor == null || floor < 0) {
            throw new IllegalArgumentException("Floor cannot be null.");
        }
    }

    @Override
    public String toString() {
        return String.format("Room: %s (Floor: %d, Device: %s)", roomNumber, floor, deviceHardwareId);
    }
}
