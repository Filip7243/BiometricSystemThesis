package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.FingerCreationRequest;
import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.dto.UpdateFingerprintRequest;
import com.bio.bio_backend.model.FingerType;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.model.Role;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.UserRepository;
import com.bio.bio_backend.utils.EncryptionUtils;
import com.neurotec.biometrics.NSubject;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.bio.bio_backend.mapper.FingerprintMapper.toDTOS;

/**
 * Serwis zarządzający operacjami na odciskach linii papilarnych.
 * <p>
 * Klasa odpowiada za obsługę operacji związanych z odciskami palców, takich jak dodawanie,
 * aktualizowanie czy wyszukiwanie odcisków palców użytkowników na podstawie typu palca i roli.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class FingerprintService {

    private final FingerprintRepository fingerprintRepository;
    private final UserRepository userRepository;
    private final EnrollmentService enrollmentService;

    /**
     * Pobiera listę wszystkich odcisków palców w systemie.
     *
     * @return Lista obiektów DTO odcisków palców.
     */
    public List<FingerprintDTO> getAllFingerprints() {
        return toDTOS(fingerprintRepository.findAll());
    }

    /**
     * Dodaje nowy odcisk palca dla użytkownika.
     *
     * @param request Obiekt żądania zawierający dane odcisku palca.
     */
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

    /**
     * Aktualizuje istniejący odcisk palca na podstawie żądania.
     *
     * @param request Obiekt żądania zawierający dane do aktualizacji odcisku palca.
     */
    @Transactional
    public void updateFingerprint(UpdateFingerprintRequest request) {
        var fingerprint = fingerprintRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("Fingerprint with id " + request.id() + " not found"));

        try {
            // Odszyfrowanie przesłanych danych w żądaniu
            byte[] originalImage = EncryptionUtils.decrypt(request.encryptedImage());

            // Tworzenie szablonu na podstawie odszyfrowanych danych
            CompletableFuture<NSubject> subjectFuture = enrollmentService.createTemplateFromFile(originalImage);

            subjectFuture.thenAccept(subject -> {
                System.out.println("Template decryption");
                byte[] template = subject.getTemplateBuffer().toByteArray();
                try {
                    // Szyfrowanie danych przed zapisem do bazy danych
                    byte[] encryptedTemplate = EncryptionUtils.encrypt(template);

                    fingerprint.setToken(encryptedTemplate);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            fingerprint.setOriginalImage(request.encryptedImage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wyszukuje odciski palców na podstawie typu palca i roli użytkownika.
     *
     * @param fingerType Typ palca (np. kciuk, palec wskazujący).
     * @param role       Rola użytkownika w systemie.
     * @return Lista obiektów DTO odcisków palców pasujących do kryteriów.
     */
    public List<FingerprintDTO> findByFingerTypeAndUserRole(FingerType fingerType, Role role) {
        return toDTOS(fingerprintRepository.findByFingerTypeAndUserRole(fingerType, role));
    }
}
