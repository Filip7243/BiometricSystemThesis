package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.AddRoomRequest;
import com.bio.bio_backend.dto.AssignDeviceToRoomRequest;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.dto.UpdateRoomRequest;
import com.bio.bio_backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    @PutMapping("/{id}")
    public void updateRoomWithId(@PathVariable Long id, @RequestBody UpdateRoomRequest request) {
        roomService.updateRoomWithId(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteRoomWithId(@PathVariable Long id) {
        roomService.deleteRoomWithId(id);
    }

    @PatchMapping("/{roomId}/devices/remove")
    public void removeDeviceFromRoom(@RequestBody AssignDeviceToRoomRequest request) {
        roomService.removeDeviceFromRoom(request);
    }

    @PatchMapping("/{roomId}/devices/assign")
    public void addDeviceToRoom(@RequestBody AssignDeviceToRoomRequest request) {
        roomService.assignDeviceToRoom(request);
    }

    @PostMapping
    public ResponseEntity<RoomDTO> addRoom(@RequestBody AddRoomRequest request) {
        return ResponseEntity.status(CREATED)
                .body(roomService.addRoom(request));
    }
}
