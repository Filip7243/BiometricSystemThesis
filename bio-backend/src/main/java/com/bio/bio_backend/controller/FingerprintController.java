package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.UpdateFingerprintRequest;
import com.bio.bio_backend.service.FingerprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
