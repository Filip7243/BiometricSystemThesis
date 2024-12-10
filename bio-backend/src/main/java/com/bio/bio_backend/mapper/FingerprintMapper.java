package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.model.Fingerprint;

import java.util.List;

public final class FingerprintMapper {

    private FingerprintMapper() {

    }

    public static FingerprintDTO toDTO(Fingerprint fingerprint) {
        return new FingerprintDTO(
                fingerprint.getId(),
                fingerprint.getToken(),
                fingerprint.getFingerType().name(),
                fingerprint.getUser().getId()
        );
    }

    public static List<FingerprintDTO> toDTOS(List<Fingerprint> fingerprints) {
        return fingerprints.stream()
                .map(FingerprintMapper::toDTO)
                .toList();
    }
}
