package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.FingerCreationRequest;
import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FingerprintService {

    private final FingerprintRepository fingerprintRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addFingerprint(FingerCreationRequest fingerCreationRequest) {
        var user = userRepository.findById(fingerCreationRequest.userId())
                .orElseThrow(() -> new IllegalArgumentException("User with id " + fingerCreationRequest.userId() + " not found"));

        // TODO: check if user has this fingerpritn in db!
        var fingerprint = new Fingerprint(fingerCreationRequest.token(), fingerCreationRequest.fingerType(), user);
        fingerprintRepository.save(fingerprint);
    }


    public List<FingerprintDTO> getAllFingerprints() {
        return fingerprintRepository.findAll()
                .stream()
                .map(f -> new FingerprintDTO(f.getToken(), f.getFingerType().name(), f.getUser().getId()))
                .toList();
    }
}
