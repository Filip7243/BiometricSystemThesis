package com.example.client.dto;

public record DeviceDTO(Long id, String macAddress, String scannerSerialNumber) {

    public DeviceDTO {
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("MacAddress cannot be null or empty.");
        }

        if (scannerSerialNumber == null || scannerSerialNumber.isBlank()) {
            throw new IllegalArgumentException("DeviceSerialNumber cannot be null or empty.");
        }
    }
}
