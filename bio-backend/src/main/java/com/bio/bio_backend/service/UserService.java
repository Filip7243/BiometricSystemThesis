package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.model.*;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FingerprintRepository fingerprintRepository;

    @Transactional
    public void addUserWithFingerprintsAndRooms(
            String firstName,
            String lastName,
            String pesel,
            Role role,
            Map<FingerType, byte[]> fingerprintData,
            List<Long> roomIds) {
        User user = new User(firstName, lastName, pesel, role);

        if (fingerprintData != null) {
            fingerprintData.forEach((fingerType, token) -> {
                Fingerprint fingerprint = new Fingerprint(token, fingerType, user);
                System.out.println(fingerprint);
                user.getFingerprints().add(fingerprint);
            });
        }

        if (roomIds != null) {
            roomIds.forEach(roomId -> {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));
                user.addRoomToUser(room);
            });
        }

        userRepository.save(user);
    }

    @Transactional
    public User updateUserRooms(Long userId, List<Long> roomIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        new HashSet<>(user.getRooms()).forEach(user::removeRoomFromUser);

        if (roomIds != null) {
            roomIds.forEach(roomId -> {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));
                user.addRoomToUser(room);
            });
        }

        return userRepository.save(user);
    }

    @Transactional
    public void addFingerprintToUser(Long userId, Long fingerprintId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Fingerprint fingerprint = fingerprintRepository.findById(fingerprintId)
                .orElseThrow(() -> new EntityNotFoundException("Fingerprint not found with id: " + fingerprintId));
        fingerprint.setUser(user);
        user.getFingerprints().add(fingerprint);

        userRepository.save(user);
    }
}
