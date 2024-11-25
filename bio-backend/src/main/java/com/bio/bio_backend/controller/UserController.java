package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.respository.UserRepository;
import com.bio.bio_backend.service.FingerprintService;
import com.bio.bio_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FingerprintService fingerprintService;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserCreationRequest userCreationRequest) {
        System.out.println(userCreationRequest);
        userService.addUserWithFingerprintsAndRooms(
                userCreationRequest.firstName(),
                userCreationRequest.lastName(),
                userCreationRequest.pesel(),
                userCreationRequest.role(),
                userCreationRequest.fingerprintData(),
                userCreationRequest.roomIds()
        );

        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/fingerprint")
    public ResponseEntity<Void> addFingerprint(@RequestBody FingerCreationRequest fingerCreationRequest) {
        fingerprintService.addFingerprint(fingerCreationRequest);
        return ResponseEntity.status(CREATED).build();
    }

    // TODO: it not should be here!
    @GetMapping("/fingerprint")
    public ResponseEntity<List<FingerprintDTO>> getAllFingerprints() {
        List<FingerprintDTO> all = fingerprintService.getAllFingerprints();
        return ResponseEntity.status(OK).body(all);
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest request) {
        userService.updateUser(request);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUserWithId(id);
    }

    @GetMapping("/{userId}/rooms")
    public ResponseEntity<List<RoomDTO>> getUserRooms(@PathVariable Long userId) {
        List<RoomDTO> rooms = userService.getUserRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    @PatchMapping("/{userId}/rooms/{roomId}")
    public void detachUserFromRoom(@PathVariable Long userId, @PathVariable Long roomId) {
        userService.detachUserFromRoom(userId, roomId);
    }
}
