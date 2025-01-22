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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.bio.bio_backend.mapper.BuildingMapper.toDTO;
import static com.bio.bio_backend.mapper.BuildingMapper.toDTOS;
import static java.util.stream.Collectors.groupingBy;

/**
 * Serwis zarządzający operacjami na budynkach.
 * <p>
 * Klasa odpowiada za obsługę operacji CRUD na budynkach, takich jak tworzenie, edytowanie, usuwanie, wyszukiwanie,
 * a także zarządzanie przypisanymi pokojami i urządzeniami.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;

    public List<BuildingDTO> getAllBuildings() {
        return toDTOS(buildingRepository.findAll());
    }

    /**
     * Aktualizuje dane budynku o podanym identyfikatorze.
     *
     * @param buildingId Identyfikator budynku.
     * @param request    Obiekt żądania zawierający nowe dane budynku.
     * @throws EntityNotFoundException Jeśli budynek o podanym ID nie istnieje.
     */
    @Transactional
    public void updateBuildingWithId(Long buildingId, UpdateBuildingRequest request) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + buildingId + " not found"));

        building.setBuildingNumber(request.buildingNumber());
        building.setStreet(request.street());
    }

    /**
     * Usuwa budynek o podanym identyfikatorze.
     * <p>
     * Przed usunięciem usuwa powiązania pokoi i urządzeń z użytkownikami.
     * </p>
     *
     * @param buildingId Identyfikator budynku.
     * @throws EntityNotFoundException Jeśli budynek o podanym ID nie istnieje.
     */
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

    /**
     * Pobiera budynek o podanym identyfikatorze.
     *
     * @param buildingId Identyfikator budynku.
     * @return Obiekt DTO budynku.
     * @throws EntityNotFoundException Jeśli budynek o podanym ID nie istnieje.
     */
    @Transactional(readOnly = true)
    public BuildingDTO getBuildingById(Long buildingId) {
        var building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + buildingId + " not found"));

        return toDTO(building);
    }

    /**
     * Pobiera listę budynków, które nie zostały przypisane do użytkownika o podanym ID.
     *
     * @param userId Identyfikator użytkownika.
     * @return Lista obiektów DTO budynków.
     */
    @Transactional(readOnly = true)
    public List<BuildingDTO> getAllBuildingsNotAssignedToUser(Long userId) {
        var allRoomsNotAssignedToUser = buildingRepository.findAllRoomsNotAssignedToUser(userId);

        Map<Long, List<Room>> roomsInBuilding = allRoomsNotAssignedToUser.stream()
                .collect(groupingBy(room -> room.getBuilding().getId()));

        return toDTOS(roomsInBuilding);
    }

    /**
     * Tworzy nowy budynek na podstawie podanych danych.
     *
     * @param request Obiekt żądania zawierający dane nowego budynku i pokoi.
     * @return Obiekt DTO nowo utworzonego budynku.
     * @throws IllegalArgumentException Jeśli urządzenie jest już przypisane do innego pokoju.
     */
    @Transactional
    public BuildingDTO createBuilding(CreateBuildingRequest request) {
        var building = new Building(request.buildingNumber(), request.street());

        List<Room> rooms = request.rooms()
                .stream()
                .map(room -> {
                    Boolean isDeviceExists = deviceRepository.existsByMacAddress(room.macAddress());

                    Device device;
                    if (!isDeviceExists) {
                        device = new Device(room.macAddress(), null, room.scannerSerialNumber());
                        deviceRepository.save(device);
                    } else {
                        device = deviceRepository.findByMacAddress(room.macAddress()).get();

                        if (device.getRoom() != null) {
                            throw new IllegalArgumentException("Device with mac address " + room.macAddress() + " is already assigned to room");
                        }
                    }

                    return new Room(
                            room.roomNumber(),
                            room.floor(),
                            building,
                            device
                    );
                }).toList();

        building.setRooms(new HashSet<>(rooms));

        Building newBuilding = buildingRepository.save(building);
        roomRepository.saveAll(rooms);

        return toDTO(newBuilding);
    }

    /**
     * Wyszukuje budynki na podstawie frazy wprowadzanej przez użytkownika.
     *
     * @param search Fraza wyszukiwana w danych budynków.
     * @return Lista obiektów DTO budynków spełniających kryteria wyszukiwania.
     */
    public List<BuildingDTO> searchBuildings(String search) {
        return toDTOS(buildingRepository.searchByFields(search));
    }
}
