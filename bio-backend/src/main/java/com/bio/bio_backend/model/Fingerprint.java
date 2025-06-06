package com.bio.bio_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Fingerprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    @Column(name = "token", columnDefinition = "BLOB")
    private byte[] token;
    @Lob
    @Column(name = "original_image", columnDefinition = "MEDIUMBLOB")
    private byte[] originalImage;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private FingerType fingerType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Fingerprint(byte[] token, FingerType fingerType,
                       User user, byte[] originalImage) {
        this.token = token;
        this.fingerType = fingerType;
        this.user = user;
        this.originalImage = originalImage;
    }
}
