package com.example.client.dto;

public record AssignDeviceToRoomRequest(Long roomId, String hardwareDeviceId) {
    public AssignDeviceToRoomRequest {
        if (roomId <= 0) {
            throw new IllegalArgumentException("Room ID must be a positive number.");
        }

        if (hardwareDeviceId == null || hardwareDeviceId.isBlank()) {
            throw new IllegalArgumentException("Hardware device ID cannot be empty or blank.");
        }
    }
}
