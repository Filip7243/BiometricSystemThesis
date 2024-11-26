package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.BuildingDTO;
import com.bio.bio_backend.dto.UpdateBuildingRequest;
import com.bio.bio_backend.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public ResponseEntity<List<BuildingDTO>> getAllBuildings() {
        List<BuildingDTO> all = buildingService.getAllBuildings();
        return ResponseEntity.ok(all);  // TODO: GROUP_BY STREET
    }

    @PutMapping("/{id}")
    public void updateBuildingWithId(@PathVariable Long id, @RequestBody UpdateBuildingRequest request) {
        buildingService.updateBuildingWithId(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuildingWithId(@PathVariable Long id) {
        return buildingService.deleteBuildingWithId(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingDTO> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @GetMapping("/rooms/not-assigned/{userId}")
    public ResponseEntity<List<BuildingDTO>> getAllBuildingsNotAssignedToUser(@PathVariable Long userId) {
        List<BuildingDTO> all = buildingService.getAllBuildingsNotAssignedToUser(userId);
        return ResponseEntity.ok(all);
    }
}
