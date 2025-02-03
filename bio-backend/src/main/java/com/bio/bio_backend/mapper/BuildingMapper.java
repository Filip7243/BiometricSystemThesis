package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.BuildingDTO;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.model.Building;
import com.bio.bio_backend.model.Room;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Klasa do mapowania obiektów typu {@link Building} na ich odpowiedniki DTO ({@link BuildingDTO}).
 * Zawiera metody statyczne, które przekształcają obiekty typu {@link Building} oraz kolekcje tych obiektów na obiekty DTO.
 */
public final class BuildingMapper {

    private BuildingMapper() {
    }

    public static BuildingDTO toDTO(Building building) {
        return new BuildingDTO(
                building.getId(),
                building.getBuildingNumber(),
                building.getStreet(),
                building.getRooms()
                        .stream()
                        .map(RoomMapper::toDTO)
                        .toList()
        );
    }

    public static List<BuildingDTO> toDTOS(List<Building> buildings) {
        return buildings
                .stream()
                .map(BuildingMapper::toDTO)
                .toList();
    }

    public static List<BuildingDTO> toDTOS(Map<Long, List<Room>> roomsInBuilding) {
        return roomsInBuilding.values().stream()
                .map(rooms -> new BuildingDTO(
                        rooms.get(0).getBuilding().getId(),
                        rooms.get(0).getBuilding().getBuildingNumber(),
                        rooms.get(0).getBuilding().getStreet(),
                        rooms.stream()
                                .map(room -> new RoomDTO(
                                        room.getId(),
                                        room.getRoomNumber(),
                                        room.getFloor(),
                                        room.getDevice() != null ? room.getDevice().getMacAddress() : null,
                                        room.getDevice() != null ? room.getDevice().getScannerSerialNumber() : null
                                ))
                                .collect(toList())
                )).collect(toList());
    }
}
