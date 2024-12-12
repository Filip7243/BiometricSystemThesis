package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.mapper.FingerprintMapper;
import com.bio.bio_backend.mapper.UserMapper;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.model.Role;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.model.User;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bio.bio_backend.mapper.RoomMapper.toDTOS;
import static com.bio.bio_backend.mapper.UserMapper.toDTOS;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FingerprintRepository fingerprintRepository;

    @Transactional
    public void addUserWithFingerprintsAndRooms(UserCreationRequest request) {
        var user = new User(request.firstName(), request.lastName(), request.pesel(), request.role());

        if (request.fingerprintTokenData() != null) {
            request.fingerprintTokenData().forEach((fingerType, token) -> {
                Fingerprint fingerprint = new Fingerprint(
                        token,
                        fingerType,
                        user,
                        request.fingerprintImageData()
                                .get(fingerType)
                );
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

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return toDTOS(userRepository.findAll());
    }

    @Transactional
    public void updateUser(UpdateUserRequest request) {
        var user = userRepository.findById(request.id())
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

    public List<FingerprintDTO> getUserFingerprints(Long userId) {
        List<Fingerprint> userFingerprints = fingerprintRepository.findByUserId(userId);


        return FingerprintMapper.toDTOS(userFingerprints);
    }

    public UserDTO getUserById(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return UserMapper.toDTO(user);
    }

    public List<UserDTO> searchUsers(String search) {
        return toDTOS(userRepository.searchByFields(search));
    }
}
