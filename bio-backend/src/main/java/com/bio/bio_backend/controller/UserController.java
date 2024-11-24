package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.FingerCreationRequest;
import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.dto.UserCreationRequest;
import com.bio.bio_backend.service.FingerprintService;
import com.bio.bio_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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

    @GetMapping("/fingerprint")
    public ResponseEntity<List<FingerprintDTO>> getAllFingerprints() {
        List<FingerprintDTO> all = fingerprintService.getAllFingerprints();
        return ResponseEntity.status(OK).body(all);
    }
}
