package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.BuildingDTO;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.dto.UpdateBuildingRequest;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.model.User;
import com.bio.bio_backend.respository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public List<BuildingDTO> getAllBuildings() {
        return buildingRepository.findAll()
                .stream()
                .map(b -> new BuildingDTO(b.getId(), b.getBuildingNumber(), b.getStreet(), b.getRooms().stream()
                        .map(r -> new RoomDTO(r.getId(), r.getRoomNumber(), r.getFloor(), r.getDevice() != null ? r.getDevice().getDeviceHardwareId() : null))
                        .toList()))
                .toList();
    }

    @Transactional
    public void updateBuildingWithId(Long buildingId, UpdateBuildingRequest request) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building with id " + buildingId + " not found"));

        building.setBuildingNumber(request.buildingNumber());
        building.setStreet(request.street());
    }

    @Transactional
    public ResponseEntity<Void> deleteBuildingWithId(Long buildingId) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building with id " + buildingId + " not found"));

        for (Room room : building.getRooms()) {
            room.removeDevice();
            room.detachUsers();
        }

        buildingRepository.deleteById(buildingId);
        return ResponseEntity.noContent().build();
    }

    @Transactional(readOnly = true)
    public BuildingDTO getBuildingById(Long buildingId) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new IllegalArgumentException("Building with id " + buildingId + " not found"));

        return new BuildingDTO(building.getId(), building.getBuildingNumber(), building.getStreet(), building.getRooms().stream()
                .map(r -> new RoomDTO(r.getId(), r.getRoomNumber(), r.getFloor(), r.getDevice() != null ? r.getDevice().getDeviceHardwareId() : null))
                .toList());
    }
}
