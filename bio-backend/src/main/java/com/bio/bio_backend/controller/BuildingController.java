package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.BuildingDTO;
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
}
