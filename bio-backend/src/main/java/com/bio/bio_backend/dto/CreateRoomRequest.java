package com.bio.bio_backend.dto;

public record CreateRoomRequest(String roomNumber,
                                Integer floor,
                                String macAddress,
                                String scannerSerialNumber) {
    public CreateRoomRequest {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be null or empty.");
        }

        if (floor == null || floor < 0) {
            throw new IllegalArgumentException("Floor cannot be null.");
        }

        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("MAC address cannot be null or empty.");
        }

        if (scannerSerialNumber == null || scannerSerialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number cannot be null or empty.");
        }
    }
}
