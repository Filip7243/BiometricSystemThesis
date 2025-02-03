package com.example.client.dto;

import com.example.model.FingerType;
import com.example.model.Role;

import java.util.List;
import java.util.Map;

public record UserCreationRequest(String firstName,
                                  String lastName,
                                  String pesel,
                                  Role role,
                                  Map<FingerType, byte[]> fingerprintImageData,
                                  List<Long> roomIds) {
    public UserCreationRequest {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty or blank.");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty or blank.");
        }

        if (pesel == null || pesel.isBlank() || !isValidPesel(pesel)) {
            throw new IllegalArgumentException("Invalid PESEL number.");
        }

        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }

        if (fingerprintImageData == null || fingerprintImageData.isEmpty()) {
            throw new IllegalArgumentException("Fingerprint data cannot be empty.");
        }

        if (roomIds == null || roomIds.isEmpty()) {
            throw new IllegalArgumentException("Room IDs cannot be empty.");
        }
    }

    private boolean isValidPesel(String pesel) {
        return true;
    }
}