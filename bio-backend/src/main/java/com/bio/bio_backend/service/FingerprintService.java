package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.FingerCreationRequest;
import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.dto.UpdateFingerprintRequest;
import com.bio.bio_backend.model.FingerType;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.model.Role;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bio.bio_backend.mapper.FingerprintMapper.toDTOS;

@Service
@RequiredArgsConstructor
public class FingerprintService {

    private final FingerprintRepository fingerprintRepository;
    private final UserRepository userRepository;

    public List<FingerprintDTO> getAllFingerprints() {
        return toDTOS(fingerprintRepository.findAll());
    }

    @Transactional
    public void addFingerprint(FingerCreationRequest request) {
        var user = userRepository.findById(request.userId())
                .orElseThrow(
                        () -> new EntityNotFoundException("User with id " + request.userId() + " not found")
                );

        fingerprintRepository.findByFingerTypeAndUser(request.fingerType(), user)
                .ifPresentOrElse(
                        f -> f.setToken(request.token()),
                        () -> {
                            var fingerprint = new Fingerprint(
                                    request.token(),
                                    request.fingerType(),
                                    user,
                                    request.originalImage()
                            );
                            fingerprintRepository.save(fingerprint);
                        });
    }

    @Transactional
    public void updateFingerprint(UpdateFingerprintRequest request) {
        var fingerprint = fingerprintRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Fingerprint with id " + request.id() + " not found"));

        fingerprint.setToken(request.token());
        fingerprint.setOriginalImage(request.originalImage());
    }

    public List<FingerprintDTO> findByFingerTypeAndUserRole(FingerType fingerType, Role role) {
        return toDTOS(fingerprintRepository.findByFingerTypeAndUserRole(fingerType, role));
    }
}
