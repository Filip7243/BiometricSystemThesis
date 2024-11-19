package com.bio.bio_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Building {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String buildingNumber;
    private String street;

    public Building(String buildingNumber, String street) {
        this.buildingNumber = buildingNumber;
        this.street = street;
    }
}
