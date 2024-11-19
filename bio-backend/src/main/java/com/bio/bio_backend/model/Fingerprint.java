package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Fingerprint {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String token;
    @Enumerated(STRING)
    private FingerType fingerType;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Fingerprint(String token, FingerType fingerType, User user) {
        this.token = token;
        this.fingerType = fingerType;
        this.user = user;
    }
}
