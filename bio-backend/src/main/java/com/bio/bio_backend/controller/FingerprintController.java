package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.dto.UpdateFingerprintRequest;
import com.bio.bio_backend.model.FingerType;
import com.bio.bio_backend.model.Role;
import com.bio.bio_backend.service.FingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fingerprints")
public class FingerprintController {

    private final FingerprintService fingerprintService;

    @PatchMapping
    public void updateFingerprint(@RequestBody UpdateFingerprintRequest request) {
        System.out.println("Updating fingerprint with id: " + request.id());
        fingerprintService.updateFingerprint(request);
    }

    @GetMapping()
    public List<FingerprintDTO> getFingerprintsByTypeAndUserRole(
            @RequestParam("fingerType") FingerType fingerType,
            @RequestParam("role") Role role) {
        return fingerprintService.findByFingerTypeAndUserRole(fingerType, role);
    }
}
