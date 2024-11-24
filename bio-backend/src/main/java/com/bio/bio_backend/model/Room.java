package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(name = "number")
    private String roomNumber;
    @Column(name = "floor")
    private Integer floor;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "building_id")
    private Building building;
    @OneToOne()
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device;
    @ManyToMany(mappedBy = "rooms")
    private Set<User> users = new HashSet<>();

    public Room(String roomNumber, Integer floor, Building building) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.building = building;
    }

    public Room(String roomNumber, Integer floor, Building building, Device device) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.building = building;
        this.device = device;
    }

    public void removeDevice() {
        if (this.device != null) {
            this.device = null;
        }
    }

    public void detachUsers() {
        for (User user : users) {
            user.getRooms().remove(this);
        }
        this.users.clear();
    }
}
