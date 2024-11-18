package com.example.model;

import java.util.regex.Pattern;

public record User(Long id, String firstName, String lastName, String pesel, Role role) {
    private static final Pattern PESEL_REGEX = Pattern.compile("\\d{11}");

    public User {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number.");
        }

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
    }

    private boolean isValidPesel(String pesel) {
        if (!PESEL_REGEX.matcher(pesel).matches()) {
            return false;
        }

        return validatePeselChecksum(pesel);
    }

    private boolean validatePeselChecksum(String pesel) {
        int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
        int checksum = 0;

        for (int i = 0; i < 10; i++) {
            checksum += Character.getNumericValue(pesel.charAt(i)) * weights[i];
        }

        int lastDigit = Character.getNumericValue(pesel.charAt(10));
        int calculatedChecksum = checksum % 10;
        int checkDigit = (10 - calculatedChecksum) % 10;

        return lastDigit == checkDigit;
    }
}
