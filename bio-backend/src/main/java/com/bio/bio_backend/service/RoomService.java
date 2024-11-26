package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.AddRoomRequest;
import com.bio.bio_backend.dto.AssignDeviceToRoomRequest;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.dto.UpdateRoomRequest;
import com.bio.bio_backend.model.Device;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.respository.BuildingRepository;
import com.bio.bio_backend.respository.DeviceRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateRoomWithId(Long id, UpdateRoomRequest request) {
        var room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room with id " + id + " not found"));

        room.setRoomNumber(request.roomNumber());
        room.setFloor(request.floor());
    }

    @Transactional
    public ResponseEntity<Void> deleteRoomWithId(Long id) {
        var room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room with id " + id + " not found"));

        room.removeDevice();
        room.detachUsers();

        roomRepository.delete(room);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public void removeDeviceFromRoom(AssignDeviceToRoomRequest request) {
        var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room with id " + request.roomId() + " not found"));

        if (!deviceRepository.existsByDeviceHardwareId(request.hardwareDeviceId())) {
            throw new IllegalArgumentException("Device with id " + request.hardwareDeviceId() + " not found");
        }

        room.removeDevice();
    }

    @Transactional
    public void assignDeviceToRoom(AssignDeviceToRoomRequest request) {
        var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room with id " + request.roomId() + " not found"));

        Device device;
        if (!deviceRepository.existsByDeviceHardwareId(request.hardwareDeviceId())) {
            device = new Device(request.hardwareDeviceId(), room);
            deviceRepository.save(device);
        } else {
            device = deviceRepository.findByDeviceHardwareId(request.hardwareDeviceId()).get();
        }

        room.setDevice(device);
    }

    @Transactional
    public RoomDTO addRoom(AddRoomRequest request) {
        var building = buildingRepository.findById(request.buildingId())
                .orElseThrow(() -> new IllegalArgumentException("Building with id " + request.buildingId() + " not found"));
        Device device;
        if (!deviceRepository.existsByDeviceHardwareId(request.deviceHardwareId())) {
            device = new Device(request.deviceHardwareId(), null);
            deviceRepository.save(device);
        } else {
            device = deviceRepository.findByDeviceHardwareId(request.deviceHardwareId()).get();
        }
        var room = new Room(request.roomNumber(), request.floor(), building, device);

        roomRepository.save(room);
        return new RoomDTO(room.getId(), room.getRoomNumber(), room.getFloor(), room.getDevice().getDeviceHardwareId());
    }

    @Transactional
    public void assignRoomToUser(Long roomId, Long userId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room with id " + roomId + " not found"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + userId + " not found"));

        user.addRoomToUser(room);
    }
}
