package com.example.client.dto;

import java.util.List;

public record UserDTO(Long id, String firstName, String lastName, String pesel, String role,
                      List<RoomDTO> assignedRooms, List<FingerprintDTO> fingerprints) {
    public UserDTO {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or blank.");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or blank.");
        }

        if (pesel == null || pesel.isBlank()) {
            throw new IllegalArgumentException("PESEL cannot be null or blank.");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank.");
        }
    }


    @Override
    public String toString() {
        return "%s %s, Pesel: %s, Role: %s".formatted(firstName, lastName, pesel, role);
    }
}
