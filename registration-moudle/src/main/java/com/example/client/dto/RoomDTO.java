package com.example.client.dto;

public record RoomDTO(Long roomId, String roomNumber) {
    public RoomDTO {
        if (roomId == null || roomId <= 0) {
            throw new IllegalArgumentException("Room ID must be a positive number.");
        }

        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be empty or blank.");
        }
    }
}
