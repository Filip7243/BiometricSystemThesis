package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.service.FingerprintService;
import com.bio.bio_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Kontroler odpowiedzialny za operacje związane z użytkownikami oraz ich odciskami palców.
 * Umożliwia tworzenie użytkowników, dodawanie odcisków palców, pobieranie danych użytkowników i ich odcisków palców,
 * przypisywanie użytkowników do pokoi oraz aktualizowanie i usuwanie użytkowników.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FingerprintService fingerprintService;

    /**
     * Tworzy nowego użytkownika z przypisanymi odciskami palców i pokojami.
     *
     * @param userCreationRequest Obiekt zawierający dane nowego użytkownika.
     * @return Status HTTP 201 (Created) wskazujący, że użytkownik został pomyślnie utworzony.
     */
    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody UserCreationRequest userCreationRequest) {
        userService.addUserWithFingerprintsAndRooms(userCreationRequest);

        return ResponseEntity.status(CREATED).build();
    }

    /**
     * Dodaje odcisk palca do systemu.
     *
     * @param fingerCreationRequest Obiekt zawierający dane odcisku palca.
     * @return Status HTTP 201 (Created) wskazujący, że odcisk palca został pomyślnie dodany.
     */
    @PostMapping("/fingerprint")
    public ResponseEntity<Void> addFingerprint(@RequestBody FingerCreationRequest fingerCreationRequest) {
        fingerprintService.addFingerprint(fingerCreationRequest);
        return ResponseEntity.status(CREATED).build();
    }

    /**
     * Pobiera wszystkie odciski palców zapisane w systemie.
     *
     * @return Lista obiektów FingerprintDTO zawierających dane odcisków palców.
     */
    @GetMapping("/fingerprint")
    public ResponseEntity<List<FingerprintDTO>> getAllFingerprints() {
        List<FingerprintDTO> all = fingerprintService.getAllFingerprints();
        return ResponseEntity.status(OK).body(all);
    }

    /**
     * Pobiera wszystkich użytkowników, opcjonalnie filtrując ich na podstawie zapytania.
     *
     * @param search Opcjonalny parametr do filtrowania użytkowników na podstawie imienia, nazwiska lub innych danych.
     * @return Lista obiektów UserDTO zawierających dane użytkowników.
     */
    @GetMapping
    public List<UserDTO> getAllUsers(@RequestParam(value = "search", required = false) String search) {
        return userService.searchUsers(search);
    }

    /**
     * Aktualizuje dane użytkownika.
     *
     * @param request Obiekt zawierający dane do zaktualizowania użytkownika.
     * @return Status HTTP 200 (OK) wskazujący, że dane użytkownika zostały pomyślnie zaktualizowane.
     */
    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest request) {
        userService.updateUser(request);
        return ResponseEntity.status(OK).build();
    }

    /**
     * Usuwa użytkownika na podstawie jego identyfikatora.
     *
     * @param id Identyfikator użytkownika do usunięcia.
     * @return Status HTTP 204 (No Content) wskazujący, że użytkownik został pomyślnie usunięty.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUserWithId(id);
    }

    /**
     * Pobiera wszystkie pokoje, do których przypisany jest użytkownik.
     *
     * @param userId Identyfikator użytkownika, dla którego mają zostać pobrane pokoje.
     * @return Lista obiektów RoomDTO zawierających dane pokoi przypisanych do użytkownika.
     */
    @GetMapping("/{userId}/rooms")
    public ResponseEntity<List<RoomDTO>> getUserRooms(@PathVariable Long userId) {
        List<RoomDTO> rooms = userService.getUserRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Odłącza użytkownika od pokoju.
     *
     * @param userId Identyfikator użytkownika, który ma zostać odłączony od pokoju.
     * @param roomId Identyfikator pokoju, z którego użytkownik ma zostać odłączony.
     */
    @PatchMapping("/{userId}/rooms/{roomId}")
    public void detachUserFromRoom(@PathVariable Long userId, @PathVariable Long roomId) {
        userService.detachUserFromRoom(userId, roomId);
    }

    /**
     * Pobiera wszystkie odciski palców przypisane do użytkownika.
     *
     * @param userId Identyfikator użytkownika, dla którego mają zostać pobrane odciski palców.
     * @return Lista obiektów FingerprintDTO zawierających dane odcisków palców przypisanych do użytkownika.
     */
    @GetMapping("/{userId}/fingerprints")
    public ResponseEntity<List<FingerprintDTO>> getUserFingerprints(@PathVariable Long userId) {
        List<FingerprintDTO> fingerprints = userService.getUserFingerprints(userId);

        return ResponseEntity.ok(fingerprints);
    }

    /**
     * Pobiera dane użytkownika na podstawie jego identyfikatora.
     *
     * @param userId Identyfikator użytkownika do pobrania.
     * @return Obiekt UserDTO zawierający dane użytkownika.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
}
