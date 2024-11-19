package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String pesel;
    @Enumerated(STRING)
    private Role role;

    @ManyToMany(cascade = {PERSIST, MERGE})
    @JoinTable(
            name = "user_rooms",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private Set<Room> rooms = new HashSet<>();

    public User(String firstName, String lastName, String pesel, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.pesel = pesel;
        this.role = role;
    }

    public void addRoomToUser(Room room) {
        rooms.add(room);
        room.getUsers().add(this);
    }

    public void removeRoomFromUser(Room room) {
        rooms.remove(room);
        room.getUsers().remove(this);
    }
}
