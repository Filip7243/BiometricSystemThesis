package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.EnrollmentRequest;
import com.bio.bio_backend.dto.EnrollmentResponse;
import com.bio.bio_backend.model.Enrollment;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.model.User;
import com.bio.bio_backend.respository.EnrollmentRepository;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.biometrics.*;
import com.neurotec.io.NBuffer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final FingerprintRepository fingerprintRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CompletableFuture<EnrollmentResponse> canUserEnterRoom(EnrollmentRequest request) throws IOException {
        FingersTools.getInstance().getClient().clear();

        NSubject subject = new NSubject();

        NFinger finger = new NFinger();
        finger.setSampleBuffer(NBuffer.fromArray(request.file().getBytes()));

        subject.getFingers().add(finger);

        NBiometricStatus status = FingersTools.getInstance()
                .getClient()
                .createTemplate(subject);

        if (status != NBiometricStatus.OK) {
            throw new IOException("Failed to create template. Status: " + status);
        }

        CompletableFuture<EnrollmentResponse> responseFuture = new CompletableFuture<>();

        List<Fingerprint> allFingerprintsByType = fingerprintRepository.findByFingerType(request.type());

        NBiometricTask enrollTask = new NBiometricTask(EnumSet.of(NBiometricOperation.ENROLL));

        for (Fingerprint fingerprint : allFingerprintsByType) {
            NBuffer buffer = new NBuffer(fingerprint.getToken());
            NSubject subjectFromDB = NSubject.fromMemory(buffer);

            Long id = fingerprint.getUser().getId();
            subjectFromDB.setId(id.toString());

            enrollTask.getSubjects().add(subjectFromDB);
        }

        FingersTools.getInstance().getClient().performTask(enrollTask);

        NBiometricStatus enrollStatus = enrollTask.getStatus();
        if (enrollStatus == NBiometricStatus.OK) {
            NBiometricStatus identifyStatus = FingersTools.getInstance().getClient().identify(subject);

            System.out.println("THRESH: " + FingersTools.getInstance().getClient().getMatchingThreshold());
            System.out.println("SPEED: " + FingersTools.getInstance().getClient().getFingersMatchingSpeed());

            if (identifyStatus == NBiometricStatus.OK) {
                for (NMatchingResult result : subject.getMatchingResults()) {
                    System.out.format("Matched with ID: '%s' with score %d\n", result.getId(), result.getScore());
                }
            } else if (identifyStatus == NBiometricStatus.MATCH_NOT_FOUND) {
                System.out.format("Match not found");
            } else {
                System.out.format("Identification failed. Status: %s\n", identifyStatus);
                return responseFuture;
            }

            if (!subject.getMatchingResults().isEmpty()) {
                NMatchingResult result = subject.getMatchingResults()
                        .stream()
                        .max(Comparator.comparing(NMatchingResult::getScore))
                        .orElseThrow(() -> new RuntimeException("No matching results"));

                User user = userRepository.findById(Long.parseLong(result.getId()))
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

                Long roomId = user.getRooms()
                        .stream()
                        .filter(r -> r.getDevice().getDeviceHardwareId().equals(request.hardwareId()))
                        .map(Room::getId)
                        .findFirst()
                        .orElse(null);

                Fingerprint fingerprint = fingerprintRepository.findByFingerTypeAndUser(request.type(), user)
                        .orElseThrow(() -> new EntityNotFoundException("Could not find finger by type and user"));

                if (roomId != null) {
                    Room room = roomRepository.findById(roomId)
                            .orElseThrow(
                                    () -> new EntityNotFoundException("Room with id: %d not found".formatted(roomId))
                            );

                    Enrollment enrollment = new Enrollment(
                            fingerprint,
                            room,
                            user,
                            true
                    );

                    enrollmentRepository.save(enrollment);

                    responseFuture.complete(new EnrollmentResponse(
                            true,
                            "Access granted",
                            user.getFirstName())
                    );
                } else {
                    Enrollment enrollment = new Enrollment(
                            fingerprint,
                            null,
                            user,
                            false
                    );

                    enrollmentRepository.save(enrollment);

                    responseFuture.complete(new EnrollmentResponse(
                            false,
                            "No permission to enter this room",
                            user.getFirstName())
                    );
                }
            } else {
                responseFuture.complete(new EnrollmentResponse(
                        false,
                        "No permission to enter this room",
                        null)
                );
            }
        } else {
            responseFuture.complete(new EnrollmentResponse(
                    false,
                    "Error when creating template",
                    null)
            );
        }

        return responseFuture;
    }
}
