package com.bio.bio_backend.db;

import com.bio.bio_backend.model.*;
import com.bio.bio_backend.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.bio.bio_backend.model.FingerType.*;
import static com.bio.bio_backend.model.Role.ADMIN;
import static com.bio.bio_backend.model.Role.USER;

@Component
public class Seeder {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private FingerprintRepository fingerprintRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private BuildingRepository buildingRepository;

//    @Override
//    public void run(String... args) throws Exception {
//        Building b1 = new Building("A0", "al. Kopisto");
//        Building b2 = new Building("A1", "al. Kopisto");
//        Building b3 = new Building("A2", "al. Kopisto");
//        buildingRepository.save(b1);
//        buildingRepository.save(b2);
//        buildingRepository.save(b3);
//
//        Room r1 = new Room("01", 0, b1);
//        Room r2 = new Room("02", 0, b1);
//        Room r3 = new Room("03", 0, b1);
//
//        Room r4 = new Room("01", 1, b2);
//        Room r5 = new Room("12", 1, b2);
//        Room r6 = new Room("23", 2, b2);
//
//        Room r7 = new Room("01", 0, b3);
//        Room r8 = new Room("02", 0, b3);
//        Room r9 = new Room("33", 3, b3);
//        roomRepository.save(r1);
//        roomRepository.save(r2);
//        roomRepository.save(r3);
//        roomRepository.save(r4);
//        roomRepository.save(r5);
//        roomRepository.save(r6);
//        roomRepository.save(r7);
//        roomRepository.save(r8);
//        roomRepository.save(r9);
//
//        User u1 = new User("Jan", "Kowalski", "12345678901", ADMIN);
//        userRepository.save(u1);
//
//        User u2 = new User("Anna", "Kowalska", "12345678902", USER);
//        userRepository.save(u2);
//
//        u2.addRoomToUser(r1);
//        u2.addRoomToUser(r2);
//        u2.addRoomToUser(r3);
//        userRepository.save(u2);
//
//        User u3 = new User("Andrzej", "Majewski", "12345678903", USER);
//        userRepository.save(u3);
//
//        u3.addRoomToUser(r3);
//        u3.addRoomToUser(r4);
//        u3.addRoomToUser(r5);
//        userRepository.save(u3);
//
//        User u4 = new User("Anna", "Pawlak", "12345678904", USER);
//        userRepository.save(u4);
//
//        u4.addRoomToUser(r6);
//        u4.addRoomToUser(r7);
//        u4.addRoomToUser(r8);
//        u4.addRoomToUser(r9);
//        userRepository.save(u4);
//
//        User u5 = new User("Krzysztof", "Marzec", "12345678905", USER);
//        userRepository.save(u5);
//
//        u5.addRoomToUser(r1);
//        u5.addRoomToUser(r2);
//        u5.addRoomToUser(r3);
//        u5.addRoomToUser(r4);
//        u5.addRoomToUser(r5);
//        u5.addRoomToUser(r6);
//        u5.addRoomToUser(r7);
//        u5.addRoomToUser(r8);
//        u5.addRoomToUser(r9);
//        userRepository.save(u5);
//
//        Fingerprint f1 = new Fingerprint(new byte[]{0, 1, 2}, INDEX, u1);
//        Fingerprint f2 = new Fingerprint(new byte[]{0, 1, 2}, MIDDLE, u1);
//        Fingerprint f3 = new Fingerprint(new byte[]{0, 1, 2}, THUMB, u1);
//
//        Fingerprint f4 = new Fingerprint(new byte[]{0, 1, 2}, INDEX, u2);
//        Fingerprint f5 = new Fingerprint(new byte[]{0, 1, 2}, MIDDLE, u2);
//        Fingerprint f6 = new Fingerprint(new byte[]{0, 1, 2}, THUMB, u2);
//
//        Fingerprint f7 = new Fingerprint(new byte[]{0, 1, 2}, INDEX, u3);
//        Fingerprint f8 = new Fingerprint(new byte[]{0, 1, 2}, MIDDLE, u3);
//        Fingerprint f9 = new Fingerprint(new byte[]{0, 1, 2}, THUMB, u3);
//
//        Fingerprint f10 = new Fingerprint(new byte[]{0, 1, 2}, INDEX, u4);
//        Fingerprint f11 = new Fingerprint(new byte[]{0, 1, 2}, MIDDLE, u4);
//        Fingerprint f12 = new Fingerprint(new byte[]{0, 1, 2}, THUMB, u4);
//
//        Fingerprint f13 = new Fingerprint(new byte[]{0, 1, 2}, INDEX, u5);
//        Fingerprint f14 = new Fingerprint(new byte[]{0, 1, 2}, MIDDLE, u5);
//        Fingerprint f15 = new Fingerprint(new byte[]{0, 1, 2}, THUMB, u5);
//
//        fingerprintRepository.save(f1);
//        fingerprintRepository.save(f2);
//        fingerprintRepository.save(f3);
//        fingerprintRepository.save(f4);
//        fingerprintRepository.save(f5);
//        fingerprintRepository.save(f6);
//        fingerprintRepository.save(f7);
//        fingerprintRepository.save(f8);
//        fingerprintRepository.save(f9);
//        fingerprintRepository.save(f10);
//        fingerprintRepository.save(f11);
//        fingerprintRepository.save(f12);
//        fingerprintRepository.save(f13);
//        fingerprintRepository.save(f14);
//        fingerprintRepository.save(f15);
//
//        Device d1 = new Device(1L, UUID.randomUUID().toString(), r1);
//        d1.setRoom(r1);
//        r1.setDevice(d1);
//        Device d2 = new Device(2L, UUID.randomUUID().toString(), r2);
//        d2.setRoom(r2);
//        r2.setDevice(d2);
//        Device d3 = new Device(3L, UUID.randomUUID().toString(), r3);
//        d3.setRoom(r3);
//        r3.setDevice(d3);
//        Device d4 = new Device(4L, UUID.randomUUID().toString(), r4);
//        d4.setRoom(r4);
//        r4.setDevice(d4);
//        Device d5 = new Device(5L, UUID.randomUUID().toString(), r5);
//        d5.setRoom(r5);
//        r5.setDevice(d5);
//        Device d6 = new Device(6L, UUID.randomUUID().toString(), r6);
//        d6.setRoom(r6);
//        r6.setDevice(d6);
//        Device d7 = new Device(7L, UUID.randomUUID().toString(), r7);
//        d7.setRoom(r7);
//        r7.setDevice(d7);
//        Device d8 = new Device(8L, UUID.randomUUID().toString(), r8);
//        d8.setRoom(r8);
//        r8.setDevice(d8);
//        Device d9 = new Device(9L, UUID.randomUUID().toString(), r9);
//        d9.setRoom(r9);
//        r9.setDevice(d9);
//
//        deviceRepository.save(d1);
//        deviceRepository.save(d2);
//        deviceRepository.save(d3);
//        deviceRepository.save(d4);
//        deviceRepository.save(d5);
//        deviceRepository.save(d6);
//        deviceRepository.save(d7);
//        deviceRepository.save(d8);
//        deviceRepository.save(d9);
//
//        roomRepository.save(r1);
//        roomRepository.save(r2);
//        roomRepository.save(r3);
//        roomRepository.save(r4);
//        roomRepository.save(r5);
//        roomRepository.save(r6);
//        roomRepository.save(r7);
//        roomRepository.save(r8);
//        roomRepository.save(r9);
//
//        System.out.println("Seeded database");
//    }
}
