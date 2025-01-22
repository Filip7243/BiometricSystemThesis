package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.AddRoomRequest;
import com.bio.bio_backend.dto.AssignDeviceToRoomRequest;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.dto.UpdateRoomRequest;
import com.bio.bio_backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * Kontroler dla operacji związanych z pomieszczeniami.
 * Umożliwia tworzenie, aktualizowanie, usuwanie, przypisywanie urządzeń oraz użytkowników do pomieszczeń,
 * a także pobieranie informacji o pokojach.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    /**
     * Aktualizuje dane pokoju o podanym identyfikatorze.
     *
     * @param id     Identyfikator pokoju do zaktualizowania.
     * @param request Obiekt zawierający dane do zaktualizowania pokoju.
     */
    @PutMapping("/{id}")
    public void updateRoomWithId(@PathVariable Long id, @RequestBody UpdateRoomRequest request) {
        roomService.updateRoomWithId(id, request);
    }

    /**
     * Usuwa pokój o podanym identyfikatorze.
     *
     * @param id Identyfikator pokoju do usunięcia.
     */
    @DeleteMapping("/{id}")
    public void deleteRoomWithId(@PathVariable Long id) {
        roomService.deleteRoomWithId(id);
    }

    /**
     * Usuwa urządzenie przypisane do pokoju.
     *
     * @param request Obiekt zawierający informacje o urządzeniu, które ma zostać usunięte z pokoju.
     */
    @PatchMapping("/{roomId}/devices/remove")
    public void removeDeviceFromRoom(@RequestBody AssignDeviceToRoomRequest request) {
        roomService.removeDeviceFromRoom(request);
    }

    /**
     * Przypisuje urządzenie do pokoju.
     *
     * @param request Obiekt zawierający informacje o urządzeniu, które ma zostać przypisane do pokoju.
     */
    @PatchMapping("/{roomId}/devices/assign")
    public void addDeviceToRoom(@RequestBody AssignDeviceToRoomRequest request) {
        roomService.assignDeviceToRoom(request);
    }

    /**
     * Tworzy nowy pokój.
     *
     * @param request Obiekt zawierający dane nowego pokoju.
     * @return Obiekt RoomDTO zawierający dane utworzonego pokoju.
     */
    @PostMapping
    public ResponseEntity<RoomDTO> addRoom(@RequestBody AddRoomRequest request) {
        return ResponseEntity.status(CREATED)
                .body(roomService.addRoom(request));
    }

    /**
     * Przypisuje użytkownika do pokoju.
     *
     * @param roomId Identyfikator pokoju, do którego użytkownik ma zostać przypisany.
     * @param userId Identyfikator użytkownika, który ma zostać przypisany do pokoju.
     */
    @PatchMapping("/{roomId}/users/{userId}/assign")
    public void assignUserToRoom(@PathVariable Long roomId, @PathVariable Long userId) {
        roomService.assignRoomToUser(roomId, userId);
    }

    /**
     * Pobiera dane pokoju na podstawie jego identyfikatora.
     *
     * @param id Identyfikator pokoju do pobrania.
     * @return Obiekt RoomDTO zawierający dane pokoju.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    /**
     * Pobiera listę wszystkich pokoi.
     *
     * @return Lista obiektów RoomDTO zawierających dane wszystkich pokoi.
     */
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }
}
