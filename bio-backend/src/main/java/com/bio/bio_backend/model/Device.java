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
    @Column(name = "mac_address")
    private String macAddress;
    @Column(name = "scanner_serial_number")
    private String scannerSerialNumber;
    @OneToOne(mappedBy = "device")
    private Room room;

    public Device(String macAddress, Room room, String scannerSerialNumber) {
        this.macAddress = macAddress;
        this.scannerSerialNumber = scannerSerialNumber;
        this.room = room;
    }

    public void removeRoom() {
        if (this.room != null) {
            this.room.removeDevice();
            this.room = null;
        }
    }
}
