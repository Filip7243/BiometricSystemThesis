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

/**
 * Kontroler REST do zarządzania budynkami.
 * Umożliwia operacje takie jak pobieranie informacji o budynkach, tworzenie, aktualizowanie oraz usuwanie budynków.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    /**
     * Pobiera listę budynków, opcjonalnie przeszukując je według nazwy.
     *
     * @param search Opcjonalny parametr do wyszukiwania budynków.
     * @return Lista budynków.
     */
    @GetMapping
    public ResponseEntity<List<BuildingDTO>> getAllBuildings(
            @RequestParam(value = "search", required = false) String search) {
        List<BuildingDTO> all = buildingService.searchBuildings(search);
        return ResponseEntity.ok(all);
    }

    /**
     * Aktualizuje budynek o podanym identyfikatorze.
     *
     * @param id      Identyfikator budynku do zaktualizowania.
     * @param request Dane aktualizujące budynek.
     */
    @PutMapping("/{id}")
    public void updateBuildingWithId(@PathVariable Long id, @RequestBody UpdateBuildingRequest request) {
        buildingService.updateBuildingWithId(id, request);
    }

    /**
     * Usuwa budynek o podanym identyfikatorze.
     *
     * @param id Identyfikator budynku do usunięcia.
     * @return Odpowiedź wskazująca, że operacja zakończyła się pomyślnie.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuildingWithId(@PathVariable Long id) {
        buildingService.deleteBuildingWithId(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Pobiera szczegóły budynku na podstawie identyfikatora.
     *
     * @param id Identyfikator budynku.
     * @return Szczegóły budynku.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuildingDTO> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    /**
     * Pobiera listę budynków, które nie zostały przypisane do użytkownika o podanym identyfikatorze.
     *
     * @param userId Identyfikator użytkownika.
     * @return Lista budynków, które nie są przypisane do użytkownika.
     */
    @GetMapping("/rooms/not-assigned/{userId}")
    public ResponseEntity<List<BuildingDTO>> getAllBuildingsNotAssignedToUser(@PathVariable Long userId) {
        List<BuildingDTO> all = buildingService.getAllBuildingsNotAssignedToUser(userId);
        return ResponseEntity.ok(all);
    }

    /**
     * Tworzy nowy budynek.
     *
     * @param request Dane do utworzenia nowego budynku.
     * @return Szczegóły nowo utworzonego budynku.
     */
    @PostMapping
    public ResponseEntity<BuildingDTO> createBuilding(@RequestBody CreateBuildingRequest request) {
        return ResponseEntity.status(CREATED).body(buildingService.createBuilding(request));
    }
}
