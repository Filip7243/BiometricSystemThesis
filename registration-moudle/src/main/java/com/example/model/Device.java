package com.example.model;

public record Device(Long id, Long deviceHardwareId, Long roomId) {
    public Device {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (deviceHardwareId <= 0) {
            throw new IllegalArgumentException("Device hardware ID must be a positive number if provided.");
        }

        if (roomId == null || roomId <= 0) {
            throw new IllegalArgumentException("Room ID must be a positive number.");
        }
    }
}
