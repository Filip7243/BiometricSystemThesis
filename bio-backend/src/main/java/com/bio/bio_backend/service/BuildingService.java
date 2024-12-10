package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.BuildingDTO;
import com.bio.bio_backend.dto.CreateBuildingRequest;
import com.bio.bio_backend.dto.UpdateBuildingRequest;
import com.bio.bio_backend.model.Building;
import com.bio.bio_backend.model.Device;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.respository.BuildingRepository;
import com.bio.bio_backend.respository.DeviceRepository;
import com.bio.bio_backend.respository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.bio.bio_backend.mapper.BuildingMapper.toDTO;
import static com.bio.bio_backend.mapper.BuildingMapper.toDTOS;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;

    public List<BuildingDTO> getAllBuildings() {
        return toDTOS(buildingRepository.findAll());
    }

    @Transactional
    public void updateBuildingWithId(Long buildingId, UpdateBuildingRequest request) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + buildingId + " not found"));

        building.setBuildingNumber(request.buildingNumber());
        building.setStreet(request.street());
    }

    @Transactional
    public void deleteBuildingWithId(Long buildingId) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + buildingId + " not found"));

        for (Room room : building.getRooms()) {
            room.removeDevice();
            room.detachUsers();
        }

        buildingRepository.deleteById(buildingId);
    }

    @Transactional(readOnly = true)
    public BuildingDTO getBuildingById(Long buildingId) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + buildingId + " not found"));

        return toDTO(building);
    }

    @Transactional(readOnly = true)
    public List<BuildingDTO> getAllBuildingsNotAssignedToUser(Long userId) {
        List<Room> allRoomsNotAssignedToUser = buildingRepository.findAllRoomsNotAssignedToUser(userId);

        Map<Long, List<Room>> roomsInBuilding = allRoomsNotAssignedToUser.stream()
                .collect(groupingBy(room -> room.getBuilding().getId()));

        return toDTOS(roomsInBuilding);
    }

    @Transactional
    public BuildingDTO createBuilding(CreateBuildingRequest request) {
        var building = new Building(request.buildingNumber(), request.street());

        List<Room> rooms = request.rooms()
                .stream()
                .map(r -> {
                    Boolean isDeviceExists = deviceRepository.existsByDeviceHardwareId(r.deviceHardwareId());

                    Device device;
                    if (!isDeviceExists) {
                        device = new Device(r.deviceHardwareId(), null);
                        deviceRepository.save(device);
                    } else {
                        device = deviceRepository.findByDeviceHardwareId(r.deviceHardwareId()).get();
                    }

                    return new Room(
                            r.roomNumber(),
                            r.floor(),
                            building,
                            device
                    );
                }).toList();

        building.setRooms(new HashSet<>(rooms));

        Building newBuilding = buildingRepository.save(building);
        roomRepository.saveAll(rooms);

        return toDTO(newBuilding);
    }
}
