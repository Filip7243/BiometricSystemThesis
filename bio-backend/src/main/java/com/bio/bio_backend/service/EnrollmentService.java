package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.model.Enrollment;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.model.User;
import com.bio.bio_backend.respository.EnrollmentRepository;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import com.bio.bio_backend.utils.EncryptionUtils;
import com.bio.bio_backend.utils.FingersTools;
import com.neurotec.biometrics.*;
import com.neurotec.io.NBuffer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.bio.bio_backend.model.Role.ADMIN;
import static com.neurotec.biometrics.NBiometricOperation.ENROLL;
import static com.neurotec.biometrics.NBiometricStatus.OK;
import static java.lang.Long.parseLong;
import static java.util.Comparator.comparing;

/**
 * Klasa `EnrollmentService` obsługuje procesy związane z zapisywaniem i sprawdzaniem dostępu użytkownika
 * do określonych pomieszczeń na podstawie odcisku palca.
 * <p>
 * Główne funkcjonalności:
 * - Tworzenie szablonów biometrycznych z danych odcisku palca.
 * - Weryfikacja użytkownika na podstawie odcisków palców.
 * - Rejestrowanie informacji o dostępie użytkownika do pomieszczeń.
 * <p>
 * Komponenty:
 * - Repositories: `FingerprintRepository`, `UserRepository`, `RoomRepository`, `EnrollmentRepository`.
 * - Zewnętrzne narzędzia: `FingersTools`, `EncryptionUtils`.
 * <p>
 * Adnotacje:
 * - `@Service`: Definiuje tę klasę jako komponent serwisowy w Spring Framework.
 * - `@RequiredArgsConstructor`: Generuje konstruktor z wymaganymi zależnościami.
 * - `@Async`: Metody oznaczone tą adnotacją są uruchamiane asynchronicznie.
 * - `@Transactional`: Oznacza, że metoda powinna być uruchamiana w kontekście transakcyjnym.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final FingerprintRepository fingerprintRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Value("${admin.panel.password}")
    private String adminPanelPassword;

    /**
     * Sprawdza, czy użytkownik ma dostęp do pomieszczenia na podstawie danych biometrycznych.
     *
     * @param request Obiekt żądania zawierający dane odcisku palca, typ palca oraz ID urządzenia.
     * @return Obiekt `CompletableFuture` zawierający odpowiedź z informacją o dostępie.
     * @throws IOException W przypadku błędu tworzenia szablonu biometrycznego.
     */
    @Async
    @Transactional
    public CompletableFuture<EnrollmentResponse> canUserEnterRoom(EnrollmentRequest request) throws IOException {
        // Czyszczenie konfiguracji klienta biometrycznego
        FingersTools.getInstance().getClient().clear();

        // Tworzenie obiekty NSubject - głównego komponentu Mega Matcher służącego do przetrzymywania
        // danych biometrycznych
        NSubject subject = new NSubject();

        // Tworzenie obiektu odcisku linii papilarnych z danych przesłanych w żądaniu i deszyfrowanie
        byte[] file;
        try {
            file = EncryptionUtils.decrypt(request.file().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        NFinger finger = new NFinger();
        finger.setSampleBuffer(NBuffer.fromArray(file));

        subject.getFingers().add(finger);

        // Tworzenie szablonu biometrycznego przez silnik biometryczny zaimplementowany w Mega Matcher
        NBiometricStatus status = FingersTools.getInstance()
                .getClient()
                .createTemplate(subject);

        if (status != OK) {
            throw new IOException("Failed to create template. Status: " + status);
        }

        CompletableFuture<EnrollmentResponse> responseFuture = new CompletableFuture<>();

        // Pobieranie odcisków palców użytkowników dla określonego typu palca
        List<Fingerprint> allFingerprintsByType = fingerprintRepository.findByFingerType(request.type());

        // Tworzenie zadania (task) biometrycznego do rejestracji (enroll) skanu
        NBiometricTask enrollTask = new NBiometricTask(EnumSet.of(ENROLL));

        for (Fingerprint fingerprint : allFingerprintsByType) {
            try {
                // Odszyfrowanie szablonu z bazy danych zawierającego dane biometryczne
                byte[] decryptedToken = EncryptionUtils.decrypt(fingerprint.getToken());

                NBuffer buffer = new NBuffer(decryptedToken);
                NSubject subjectFromDB = NSubject.fromMemory(buffer);

                // Ustawienie id dla konkretnego szablonu w celu późniejszej identyfikacji go (zgodnego z id użytkownika z bd)
                Long id = fingerprint.getUser().getId();
                subjectFromDB.setId(id.toString());

                enrollTask.getSubjects().add(subjectFromDB);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        FingersTools.getInstance().getClient().performTask(enrollTask);

        NBiometricStatus enrollStatus = enrollTask.getStatus();
        if (enrollStatus == OK) {
            // Identyfikacja użytkownika
            NBiometricStatus identifyStatus = FingersTools.getInstance().getClient().identify(subject);

            if (identifyStatus == OK) {
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
                // Obsługa wyniku identyfikacji, pobieranie próbki, która miała największy matching score
                NMatchingResult result = subject.getMatchingResults()
                        .stream()
                        .max(comparing(NMatchingResult::getScore))
                        .orElseThrow(() -> new RuntimeException("No matching results"));

                User user = userRepository.findById(parseLong(result.getId()))
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

                System.out.println("HARDWARE ID: " + request.hardwareId());
                // TODO: handle lowercase hardware ID everywhere when saving etc..
                Long roomId = user.getRooms()
                        .stream()
                        .filter(r -> r.getDevice().getMacAddress().equals(request.hardwareId().toLowerCase()))
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

    public List<Object[]> findDailyEnrollmentTrend() {
        return enrollmentRepository.findDailyEnrollmentTrend();
    }

    public List<Object[]> findPeakEnrollmentHours() {
        return enrollmentRepository.findPeakEnrollmentHours();
    }

    public List<Object[]> findTopActiveUsers() {
        return enrollmentRepository.findTopActiveUsers();
    }

    public List<Object[]> getEnrollmentStatusDistribution() {
        return enrollmentRepository.getEnrollmentStatusDistribution();
    }

    public List<Object[]> getEnrollmentsByRoom() {
        return enrollmentRepository.getEnrollmentsByRoom();
    }

    public List<Object[]> getEnrollmentsPerFingerprint() {
        return enrollmentRepository.getEnrollmentsPerFingerprint();
    }

    public List<UserEnrollmentConfirmationDTO> getUserEnrollmentConfirmationRate(Long userId) {
        return enrollmentRepository.getUserEnrollmentConfirmationRate(userId);
    }

    public List<Object[]> getEnrollmentsByTimeOfDay() {
        return enrollmentRepository.getEnrollmentsByTimeOfDay();
    }

    public List<Object[]> getEnrollmentsByRoomAndStatus() {
        return enrollmentRepository.getEnrollmentsByRoomAndStatus();
    }

    public List<Object[]> getRoomUsageByUser() {
        return enrollmentRepository.getRoomUsageByUser();
    }

    public List<RoomEntranceDTO> getNumberOfEntrancesToEachRoomOnDate(LocalDate date, Long buildingId) {
        return enrollmentRepository.getNumberOfEntrancesToEachRoomOnDate(date, buildingId);
    }

    public List<UnconfirmedEntranceDTO> getUnconfirmedEntrancesPerUserByRoom() {
        return enrollmentRepository.getUnconfirmedEntrancesPerUserByRoom();
    }

    public List<LateControlDTO> getLateControlByUserAndRoom(int expectedHour, LocalDate date, Long userId) {
        return enrollmentRepository.getLateControlByUserAndRoom(date, userId, expectedHour);
    }

    /**
     * Tworzy szablon biometryczny na podstawie przesłanych danych odcisku palca.
     *
     * @param file Tablica bajtów reprezentująca dane odcisku palca.
     * @return Obiekt `CompletableFuture` zawierający szablon biometryczny lub błąd.
     */
    @Async
    public CompletableFuture<NSubject> createTemplateFromFile(byte[] file) {
        // Czyszczenie danych klienta
        FingersTools.getInstance().getClient().clear();

        NSubject subject = new NSubject();
        NFinger finger = new NFinger();
        finger.setSampleBuffer(NBuffer.fromArray(file));

        subject.getFingers().add(finger);

        // Tworzenie szablonu
        NBiometricStatus status = FingersTools.getInstance().getClient().createTemplate(subject);

        return status == OK
                ? CompletableFuture.completedFuture(subject)
                : CompletableFuture.failedFuture(new RuntimeException("Failed to create template. Status: " + status));
    }

    /**
     * Funkcja do logowania do panelu administracyjnego z wykorzystaniem biometrii.
     *
     * @param request Obiekt zawierający dane logowania, w tym plik z odciskiem palca oraz typ palca.
     * @return CompletableFuture<LoginResponse> Obiekt zawierający odpowiedź na próbę logowania (czy użytkownik jest administratorem).
     */
    @Async
    public CompletableFuture<LoginResponse> loginToAdminPanelWithBiometrics(BiometricsLoginRequest request) {
        // Czyszczenie konfiguracji klienta biometrycznego
        FingersTools.getInstance().getClient().clear();

        try {
            byte[] originalImage = EncryptionUtils.decrypt(request.file());
            CompletableFuture<NSubject> template = createTemplateFromFile(originalImage);

            return template.thenApply(subject -> {
                List<Fingerprint> allFingerprintsByType = fingerprintRepository.findByFingerType(request.type());

                // Tworzenie zadania (task) biometrycznego do rejestracji (enroll) skanu
                NBiometricTask enrollTask = new NBiometricTask(EnumSet.of(ENROLL));

                for (Fingerprint fingerprint : allFingerprintsByType) {
                    try {
                        // Odszyfrowanie szablonu z bazy danych zawierającego dane biometryczne
                        byte[] decryptedToken = EncryptionUtils.decrypt(fingerprint.getToken());

                        NBuffer buffer = new NBuffer(decryptedToken);
                        NSubject subjectFromDB = NSubject.fromMemory(buffer);

                        // Ustawienie id dla konkretnego szablonu w celu
                        // późniejszej identyfikacji go (zgodnego z id użytkownika z bd)
                        Long id = fingerprint.getUser().getId();
                        subjectFromDB.setId(id.toString());

                        enrollTask.getSubjects().add(subjectFromDB);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Something went wrong when decrypting from db: " + e.getMessage());
                    }
                }

                FingersTools.getInstance().getClient().performTask(enrollTask);

                NBiometricStatus enrollStatus = enrollTask.getStatus();

                if (enrollStatus == OK) {
                    // Identyfikacja użytkownika
                    NBiometricStatus identifyStatus = FingersTools.getInstance().getClient().identify(subject);

                    if (identifyStatus == OK && !subject.getMatchingResults().isEmpty()) {
                        // Obsługa wyniku identyfikacji, pobieranie próbki, która miała największy matching score
                        NMatchingResult result = subject.getMatchingResults()
                                .stream()
                                .max(comparing(NMatchingResult::getScore))
                                .orElseThrow(() -> new RuntimeException("No matching results"));

                        // Jeśli istnieje użytkownik o podanym ID i roli ADMIN to zwracamy true
                        return new LoginResponse(userRepository.existsByIdAndRole(parseLong(result.getId()), ADMIN));
                    }
                }

                // Jeśli enrollment/identify status nie jest OK to zwracamy false
                return new LoginResponse(false);
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Something went wrong: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when decrypting: " + e.getMessage());
        }
    }

    public LoginResponse loginToAdminPanelWithPassword(PasswordLoginRequest request) {
        try {
            // Odszyfrowanie hasła
            byte[] requestPassword = EncryptionUtils.decrypt(request.encryptedPassword());

//            String password = Arrays.toString(requestPassword);

            String password = new String(requestPassword);

            System.out.println("PASSWORD: " + password);

            return password.equals(adminPanelPassword)
                    ? new LoginResponse(true)
                    : new LoginResponse(false);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong when trying to login with password: " + e.getMessage());
        }
    }
}
