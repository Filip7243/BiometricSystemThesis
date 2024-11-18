package com.example.model;

public record Room(Long id, String roomNumber, Integer floor, Long buildingId, Long deviceId) {

    public Room {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be empty or blank.");
        }

        if (floor == null || floor <= 0) {
            throw new IllegalArgumentException("Floor number must be a positive integer.");
        }

        if (buildingId == null || buildingId <= 0) {
            throw new IllegalArgumentException("Building ID must be a positive number.");
        }

        if (deviceId <= 0) {
            throw new IllegalArgumentException("Device ID must be a positive number if provided.");
        }
    }
}
