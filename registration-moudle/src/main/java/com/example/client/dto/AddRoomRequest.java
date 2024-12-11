package com.example.client.dto;

public record AddRoomRequest(String roomNumber,
                             Integer floor,
                             String macAddress,
                             String scannerSerialNumber,
                             Long buildingId) {
    public AddRoomRequest {
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number cannot be null or empty.");
        }

        if (floor == null) {
            throw new IllegalArgumentException("Floor cannot be null.");
        }

        if (buildingId == null) {
            throw new IllegalArgumentException("BuildingId cannot be null.");
        }

        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("MacAddress cannot be null or empty.");
        }

        if (scannerSerialNumber == null || scannerSerialNumber.isBlank()) {
            throw new IllegalArgumentException("ScannerSerialNumber cannot be null or empty.");
        }
    }
}
