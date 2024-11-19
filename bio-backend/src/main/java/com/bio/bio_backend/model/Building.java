package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
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
    @Column(name = "number")
    private String buildingNumber;
    @Column(name = "street")
    private String street;
    @OneToMany(mappedBy = "building", cascade = ALL, orphanRemoval = true)
    private Set<Room> rooms = new HashSet<>();

    public Building(String buildingNumber, String street) {
        this.buildingNumber = buildingNumber;
        this.street = street;
    }
}
