package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.service.EnrollmentService;
import com.neurotec.biometrics.NSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.http.HttpStatus.CREATED;

/**
 * Kontroler REST do zarządzania procesem zapisu użytkownika do pokoju.
 * Umożliwia operację wysyłania pliku z danymi zapisu i przetwarzania tego zapisu.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * Przesyła dane biometryczne użytkownika, sprawdzając, czy użytkownik ma dostęp do pomieszczenia.
     *
     * @param request Obiekt zawierający dane zapisu użytkownika.
     * @return Odpowiedź HTTP z odpowiednim statusem i wynikami zapisu.
     */
    @PostMapping
    public ResponseEntity<?> canUserEnterRoom(@ModelAttribute EnrollmentRequest request) {
        try {
            // Rozpoczęcie asynchronicznego procesu weryfikacji użytkownika
            CompletableFuture<EnrollmentResponse> future = enrollmentService.canUserEnterRoom(request);

            // Zwrócenie odpowiedzi po zakończeniu procesu weryfikacji (maksymalny czas oczekiwania: 10 sekund)
            return ResponseEntity.ok(future.get(10, SECONDS));
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException ex) {
            // W przypadku problemu z zapisaniem danych biometrycznych lub problemem identyfikacji - zwrócenie błędu z odpowiednim komunikatem
            return ResponseEntity.internalServerError().body("Something went wrong: " + ex.getMessage());
        }
    }

    @GetMapping("/daily-trend")
    public ResponseEntity<?> findDailyEnrollmentTrend() {
        return ResponseEntity.ok(enrollmentService.findDailyEnrollmentTrend());
    }

    @GetMapping("/hours-peaks")
    public ResponseEntity<?> findPeakEnrollmentHours() {
        return ResponseEntity.ok(enrollmentService.findPeakEnrollmentHours());
    }

    @GetMapping("/most-active-users")
    public ResponseEntity<?> findTopActiveUsers() {
        return ResponseEntity.ok(enrollmentService.findTopActiveUsers());
    }

    @GetMapping("/enrollment-status-distribution")
    public ResponseEntity<?> getEnrollmentStatusDistribution() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentStatusDistribution());
    }

    @GetMapping("/enrollments-by-room")
    public ResponseEntity<?> getEnrollmentsByRoom() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByRoom());
    }

    @GetMapping("/enrollments-per-fingerprint")
    public ResponseEntity<?> getEnrollmentsPerFingerprint() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsPerFingerprint());
    }

    @GetMapping("/enrollments-by-time-of-day")
    public ResponseEntity<?> getEnrollmentsByTimeOfDay() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByTimeOfDay());
    }

    @GetMapping("/enrollments-by-room-and-status")
    public ResponseEntity<?> getEnrollmentsByRoomAndStatus() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByRoomAndStatus());
    }

    @GetMapping("/enrollments-room-usage-by-user")
    public ResponseEntity<?> getRoomUsageByUser() {
        return ResponseEntity.ok(enrollmentService.getRoomUsageByUser());
    }

    // NEW

    @GetMapping("/entrances-to-room")
    public List<RoomEntranceDTO> getEntrancesToRoom(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam("buildingId") Long buildingId) {
        return enrollmentService.getNumberOfEntrancesToEachRoomOnDate(date, buildingId);
    }

    @GetMapping("/unconfirmed-entrances-per-user-by-room")
    public List<UnconfirmedEntranceDTO> getUnconfirmedEntrancesPerUserByRoom() {
        return enrollmentService.getUnconfirmedEntrancesPerUserByRoom();
    }

    @GetMapping("/enrollments-confirmation-rate")
    public ResponseEntity<List<UserEnrollmentConfirmationDTO>> getUserEnrollmentConfirmationRate(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getUserEnrollmentConfirmationRate(userId));
    }

    @GetMapping("/late-control")
    public ResponseEntity<List<LateControlDTO>> getLateControlByUserAndRoom(@RequestParam("expectedHour") Integer expectedHour,
                                                                            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getLateControlByUserAndRoom(expectedHour, date, userId));
    }

    @PostMapping("/tests")
    public ResponseEntity<Void> test(@RequestParam("file") MultipartFile file) throws IOException {
        CompletableFuture<NSubject> statusFuture = enrollmentService.createTemplateFromFile(file.getBytes());
        statusFuture.thenAccept(status -> {
            System.out.println("Status: " + status);
        });

        return ResponseEntity.status(CREATED).build();
    }
}
