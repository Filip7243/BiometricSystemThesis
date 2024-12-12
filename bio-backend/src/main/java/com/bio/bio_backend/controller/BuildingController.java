package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.BuildingDTO;
import com.bio.bio_backend.dto.CreateBuildingRequest;
import com.bio.bio_backend.dto.UpdateBuildingRequest;
import com.bio.bio_backend.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public ResponseEntity<List<BuildingDTO>> getAllBuildings(
            @RequestParam(value = "search", required = false) String search) {
        List<BuildingDTO> all = buildingService.searchBuildings(search);
        return ResponseEntity.ok(all);
    }

    @PutMapping("/{id}")
    public void updateBuildingWithId(@PathVariable Long id, @RequestBody UpdateBuildingRequest request) {
        buildingService.updateBuildingWithId(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuildingWithId(@PathVariable Long id) {
        buildingService.deleteBuildingWithId(id);

        return ResponseEntity.noContent().build();
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

    @PostMapping
    public ResponseEntity<BuildingDTO> createBuilding(@RequestBody CreateBuildingRequest request) {
        return ResponseEntity.status(CREATED).body(buildingService.createBuilding(request));
    }
}
