package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String roomNumber;
    private Integer floor;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "building_id")
    private Building building;
    @ManyToMany(mappedBy = "rooms")
    private final Set<User> users = new HashSet<>();

    public Room(String roomNumber, Integer floor, Building building) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.building = building;
    }
}
