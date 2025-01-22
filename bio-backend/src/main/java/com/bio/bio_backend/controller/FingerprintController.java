package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.dto.UpdateFingerprintRequest;
import com.bio.bio_backend.model.FingerType;
import com.bio.bio_backend.model.Role;
import com.bio.bio_backend.service.FingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kontroler REST do zarządzania danymi odcisków palców.
 * Umożliwia aktualizację odcisków palców oraz wyszukiwanie odcisków na podstawie typu palca i roli użytkownika.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fingerprints")
public class FingerprintController {

    private final FingerprintService fingerprintService;

    /**
     * Aktualizuje dane odcisku palca na podstawie przesłanych danych.
     *
     * @param request Obiekt zawierający dane do aktualizacji odcisku palca.
     */
    @PatchMapping
    public void updateFingerprint(@RequestBody UpdateFingerprintRequest request) {
        System.out.println("Updating fingerprint with id: " + request.id());
        fingerprintService.updateFingerprint(request);
    }

    /**
     * Wyszukuje odciski palców na podstawie typu palca oraz roli użytkownika.
     *
     * @param fingerType Typ palca, na przykład kciuk, palec wskazujący itp.
     * @param role Rola użytkownika, na przykład administrator, użytkownik itp.
     * @return Lista odcisków palców odpowiadających podanym kryteriom.
     */
    @GetMapping()
    public List<FingerprintDTO> getFingerprintsByTypeAndUserRole(
            @RequestParam("fingerType") FingerType fingerType,
            @RequestParam("role") Role role) {
        return fingerprintService.findByFingerTypeAndUserRole(fingerType, role);
    }
}
