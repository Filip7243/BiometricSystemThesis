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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bio.bio_backend.mapper.RoomMapper.toDTO;

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
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + id + " not found"));

        room.setRoomNumber(request.roomNumber());
        room.setFloor(request.floor());
    }

    @Transactional
    public void deleteRoomWithId(Long id) {
        var room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + id + " not found"));

        room.removeDevice();
        room.detachUsers();
        room.removeEnrollments();

        roomRepository.delete(room);
    }

    @Transactional
    public void removeDeviceFromRoom(AssignDeviceToRoomRequest request) {
        var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + request.roomId() + " not found"));

        if (!deviceRepository.existsByMacAddress(request.macAddress())) {
            throw new EntityNotFoundException("Device with id " + request.macAddress() + " not found");
        }

        room.removeDevice();
    }

    @Transactional
    public void assignDeviceToRoom(AssignDeviceToRoomRequest request) {
        var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + request.roomId() + " not found"));

        Device device;
        if (!deviceRepository.existsByMacAddress(request.macAddress())) {
            device = new Device(request.macAddress(), room, request.scannerSerialNumber());  // TODO: do zmiany
            deviceRepository.save(device);
        } else {
            device = deviceRepository.findByMacAddress(request.macAddress()).get();
        }

        room.setDevice(device);
    }

    @Transactional
    public RoomDTO addRoom(AddRoomRequest request) {
        var building = buildingRepository.findById(request.buildingId())
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + request.buildingId() + " not found"));

        Device device;
        if (!deviceRepository.existsByMacAddress(request.macAddress())) {
            device = new Device(request.macAddress(), null, request.scannerSerialNumber());  // TODO: do zminay
            deviceRepository.save(device);
        } else {
            device = deviceRepository.findByMacAddress(request.macAddress())
                    .get();
            if (device.getRoom() != null) {
                throw new IllegalArgumentException("Device with mac address " + request.macAddress() + " is already assigned to room");
            }
        }

        var room = new Room(request.roomNumber(), request.floor(), building, device);

        roomRepository.save(room);

        return toDTO(room);
    }

    @Transactional
    public void assignRoomToUser(Long roomId, Long userId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + roomId + " not found"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        user.addRoomToUser(room);
    }

    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + roomId + " not found"));

        return toDTO(room);
    }
}
