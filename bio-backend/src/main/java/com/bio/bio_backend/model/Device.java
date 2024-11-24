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
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "hardware_device_id")
    private String deviceHardwareId;
    @OneToOne(mappedBy = "device")
    private Room room;

    public Device(String deviceHardwareId, Room room) {
        this.deviceHardwareId = deviceHardwareId;
        this.room = room;
    }

    public void removeRoom() {
        if (this.room != null) {
            this.room.removeDevice();
            this.room = null;
        }
    }
}
