package com.example.model;

public record Building(Long id, String buildingNumber, String street) { // TODO: dodac ulice w gu
    public Building {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

        if (buildingNumber == null || buildingNumber.isBlank()) {
            throw new IllegalArgumentException("Building number cannot be null or blank.");
        }

        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be null or blank.");
        }
    }
}
