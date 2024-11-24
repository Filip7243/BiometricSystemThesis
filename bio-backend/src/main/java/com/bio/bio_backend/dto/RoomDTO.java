package com.bio.bio_backend.dto;

public record RoomDTO(Long roomId, String roomNumber, Integer floor, String hardwareDeviceId) {
    public RoomDTO {
        if (roomId == null || roomId <= 0) {
            throw new IllegalArgumentException("Room ID must be a positive number.");
        }

        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room name cannot be empty or blank.");
        }

        if (floor == null || floor < 0) {
            System.out.println("FLOOR: " + floor);
            throw new IllegalArgumentException("Floor number must be a positive integer.");
        }
    }
}
