package com.example.client.dto;

public record UnconfirmedEntranceDTO(String firstName,
                                     String lastName,
                                     String roomNumber,
                                     String buildingNumber,
                                     Long count) {
}

