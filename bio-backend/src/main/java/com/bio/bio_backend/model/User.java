package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "pesel", unique = true)
    private String pesel;
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Fingerprint> fingerprints = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "users_rooms",
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
