package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.EnrollmentRequest;
import com.bio.bio_backend.dto.EnrollmentResponse;
import com.bio.bio_backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<?> uploadFile(@ModelAttribute EnrollmentRequest request) {
        try {
            CompletableFuture<EnrollmentResponse> enrollmentResponseCompletableFuture =
                    enrollmentService.canUserEnterRoom(request);

            return ResponseEntity.ok(enrollmentResponseCompletableFuture.get(10, TimeUnit.SECONDS));

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body("Could not store file: " + ex.getMessage());
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
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

    @GetMapping("/entrances-to-room")
    public List<Object[]> getEntrancesToRoom(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @RequestParam("buildingId") Long buildingId) {
        return enrollmentService.getNumberOfEntrancesToEachRoomOnDate(date, buildingId);
    }

    @GetMapping("/unconfirmed-entrances-per-user-by-room")
    public List<Object[]> getUnconfirmedEntrancesPerUserByRoom() {
        return enrollmentService.getUnconfirmedEntrancesPerUserByRoom();
    }

    @GetMapping("/enrollments-confirmation-rate")
    public ResponseEntity<?> getUserEnrollmentConfirmationRate(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getUserEnrollmentConfirmationRate(userId));
    }

    @GetMapping("/late-control")
    public ResponseEntity<?> getLateControlByUserAndRoom(@RequestParam("expectedHour") Integer expectedHour,
                                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                         @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getLateControlByUserAndRoom(expectedHour, date, userId));
    }
}
