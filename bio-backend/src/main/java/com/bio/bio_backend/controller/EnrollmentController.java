package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.EnrollmentRequest;
import com.bio.bio_backend.dto.EnrollmentResponse;
import com.bio.bio_backend.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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
}
