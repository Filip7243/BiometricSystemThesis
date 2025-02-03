package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.service.EnrollmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Kontroler REST do zarządzania procesem zapisu użytkownika do pokoju.
 * Umożliwia operację wysyłania pliku z danymi zapisu i przetwarzania tego zapisu.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enrollments")
@Tag(name = "Enrollment Controller", description = "Endpointy do zarządzania zapisami (enrollments)")
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
            // W przypadku problemu z zapisaniem danych biometrycznych lub problemem identyfikacji-
            // zwrócenie błędu z odpowiednim komunikatem
            return ResponseEntity.internalServerError().body("Something went wrong: " + ex.getMessage());
        }
    }

    /**
     * Przesyła dane biometryczne użytkownika, sprawdzając, czy użytkownik ma dostęp do panelu administratora.
     *
     * @param request Obiekt zawierający dane biometryczne administratora.
     * @return Odpowiedź HTTP z odpowiednim statusem i wynikam logowania.
     */
    @PostMapping("/login-biometrics")
    public ResponseEntity<LoginResponse> loginToAdminPanelWithBiometrics(@RequestBody BiometricsLoginRequest request) {
        try {
            // Rozpoczęcie asynchronicznego procesu weryfikacji użytkownika
            CompletableFuture<LoginResponse> future = enrollmentService.loginToAdminPanelWithBiometrics(request);

            // Zwrócenie odpowiedzi po zakończeniu procesu weryfikacji (maksymalny czas oczekiwania: 10 sekund)
            return ResponseEntity.ok(future.get(10, SECONDS));
        } catch (ExecutionException | InterruptedException | TimeoutException ex) {
            // W przypadku problemu z przetworzeniem danych biometrycznych lub problemem identyfikacji-
            // zwrócenie błędu z odpowiednim komunikatem
            return ResponseEntity.internalServerError().body(new LoginResponse(false));
        }
    }

    /**
     * Loguje użytkownika do panelu administratora za pomocą hasła.
     *
     * @param request Obiekt zawierający dane logowania (np. login i hasło) wymagane do uwierzytelnienia.
     * @return Odpowiedź HTTP zawierająca obiekt {@link LoginResponse} z wynikiem logowania.
     */
    @PostMapping("/login-password")
    public ResponseEntity<LoginResponse> loginToAdminPanelWithPassword(@RequestBody PasswordLoginRequest request) {
        return ResponseEntity.ok(enrollmentService.loginToAdminPanelWithPassword(request));
    }

    /**
     * Pobiera listę wejść do poszczególnych pomieszczeń w danym budynku na określony dzień.
     *
     * @param date       Data, dla której mają zostać zwrócone dane wejść (w formacie ISO DATE).
     * @param buildingId Identyfikator budynku, dla którego mają zostać zwrócone dane wejść.
     * @return Lista obiektów DTO zawierających informacje o wejściach do pomieszczeń.
     */
    @GetMapping("/entrances-to-room")
    public List<RoomEntranceDTO> getEntrancesToRoom(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                    @RequestParam("buildingId") Long buildingId) {
        return enrollmentService.getNumberOfEntrancesToEachRoomOnDate(date, buildingId);
    }

    /**
     * Pobiera listę niepotwierdzonych wejść użytkowników, pogrupowanych według pomieszczeń.
     *
     * @return Lista obiektów DTO zawierających informacje o niepotwierdzonych wejściach użytkowników do poszczególnych pomieszczeń.
     */
    @GetMapping("/unconfirmed-entrances-per-user-by-room")
    public List<UnconfirmedEntranceDTO> getUnconfirmedEntrancesPerUserByRoom() {
        return enrollmentService.getUnconfirmedEntrancesPerUserByRoom();
    }

    /**
     * Pobiera współczynnik potwierdzeń zapisów (enrollment) dla danego użytkownika.
     *
     * @param userId Identyfikator użytkownika, dla którego ma zostać obliczony współczynnik potwierdzeń.
     * @return Odpowiedź HTTP zawierająca listę obiektów DTO z informacjami o współczynniku potwierdzeń zapisów użytkownika.
     */
    @GetMapping("/enrollments-confirmation-rate")
    public ResponseEntity<List<UserEnrollmentConfirmationDTO>> getUserEnrollmentConfirmationRate(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getUserEnrollmentConfirmationRate(userId));
    }

    /**
     * Pobiera informacje o spóźnieniach użytkownika w danym pomieszczeniu na określony dzień i godzinę.
     *
     * @param expectedHour Godzina, do której użytkownik powinien był wejść do pomieszczenia.
     * @param date         Data, dla której mają zostać zwrócone dane o spóźnieniach (w formacie ISO DATE).
     * @param userId       Identyfikator użytkownika, dla którego mają zostać zwrócone dane o spóźnieniach.
     * @return Odpowiedź HTTP zawierająca listę obiektów DTO z informacjami o spóźnieniach użytkownika.
     */
    @GetMapping("/late-control")
    public ResponseEntity<List<LateControlDTO>> getLateControlByUserAndRoom(@RequestParam("expectedHour") Integer expectedHour,
                                                                            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(enrollmentService.getLateControlByUserAndRoom(expectedHour, date, userId));
    }
}
