package com.example.client.dto;

public record RoomDTO(Long roomId,
                      String roomNumber,
                      Integer floor,
                      String macAddress,
                      String scannerSerialNumber) {
    public RoomDTO {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be empty or blank.");
        }

        if (floor == null || floor < 0) {
            throw new IllegalArgumentException("Floor number must be a positive integer.");
        }
    }

    @Override
    public String toString() {
        return "Room ID: %d, Room Number: %s, Floor: %d".formatted(roomId, roomNumber, floor);
    }
}
