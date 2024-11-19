package com.bio.bio_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    private Long id;
    private Long deviceHardwareId;
    @MapsId
    @OneToOne(fetch = LAZY)
    private Room room;

    public Device(Long deviceHardwareId, Room room) {
        this.deviceHardwareId = deviceHardwareId;
        this.room = room;
    }
}
