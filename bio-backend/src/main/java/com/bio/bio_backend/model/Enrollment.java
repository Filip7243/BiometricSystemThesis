package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "fingerprint_id")
    private Fingerprint fingerprint;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private Boolean isConfirmed;
    @CreationTimestamp
    private Timestamp enrollmentDate;

    public Enrollment(Fingerprint fingerprint, Room room, User user, Boolean isConfirmed) {
        this.fingerprint = fingerprint;
        this.room = room;
        this.user = user;
        this.isConfirmed = isConfirmed;
    }
}
