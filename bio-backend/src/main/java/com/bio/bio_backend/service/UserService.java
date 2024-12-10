package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.mapper.RoomMapper;
import com.bio.bio_backend.model.*;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.bio.bio_backend.mapper.RoomMapper.toDTOS;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FingerprintRepository fingerprintRepository;

    @Transactional
    public void addUserWithFingerprintsAndRooms(UserCreationRequest request) {
        User user = new User(request.firstName(), request.lastName(), request.pesel(), request.role());

        if (request.fingerprintData() != null) {
            request.fingerprintData().forEach((fingerType, token) -> {
                Fingerprint fingerprint = new Fingerprint(token, fingerType, user);
                System.out.println(fingerprint);
                user.getFingerprints().add(fingerprint);
            });
        }

        if (request.roomIds() != null) {
            request.roomIds().forEach(roomId -> {
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

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getPesel(),
                        u.getRole().name(),
                        u.getRooms()
                                .stream()
                                .map(r -> new RoomDTO(
                                        r.getId(),
                                        r.getRoomNumber(),
                                        r.getFloor(),
                                        r.getDevice().getDeviceHardwareId()))
                                .toList(),
                        u.getFingerprints()
                                .stream()
                                .map(f -> new FingerprintDTO(
                                        f.getId(),
                                        f.getToken(),
                                        f.getFingerType().name(),
                                        f.getUser().getId()))
                                .toList())
                ).collect(toList());
    }

    @Transactional
    public void updateUser(UpdateUserRequest request) {
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.id()));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPesel(request.pesel());
        user.setRole(Role.valueOf(request.role()));
    }

    @Transactional
    public ResponseEntity<Void> deleteUserWithId(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        System.out.println("User with id: " + id + " deleted");
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getUserRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return toDTOS(user.getRooms());
    }

    @Transactional
    public void detachUserFromRoom(Long userId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));

        user.removeRoomFromUser(room);
    }
}
