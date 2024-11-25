package com.bio.bio_backend.dto;

public record UpdateUserRequest(Long id, String firstName, String lastName, String pesel, String role) {
    public UpdateUserRequest {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty or blank.");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty or blank.");
        }

        if (pesel == null || pesel.isBlank()) {
            throw new IllegalArgumentException("PESEL cannot be empty or blank.");
        }

        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty or blank.");
        }
    }
}
