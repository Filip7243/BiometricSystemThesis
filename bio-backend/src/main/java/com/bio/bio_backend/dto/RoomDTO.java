package com.bio.bio_backend.dto;

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
}
