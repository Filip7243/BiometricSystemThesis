package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.model.Room;

import java.util.List;
import java.util.Set;

/**
 * Klasa do mapowania obiektów typu {@link Room} na ich odpowiedniki DTO ({@link RoomDTO}).
 * Zawiera metody statyczne, które przekształcają obiekty typu {@link Room} oraz kolekcje tych obiektów na obiekty DTO.
 */
public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomDTO toDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getDevice() != null ? room.getDevice().getMacAddress() : null,
                room.getDevice() != null ? room.getDevice().getScannerSerialNumber() : null
        );
    }

    public static List<RoomDTO> toDTOS(Set<Room> rooms) {
        return rooms.stream()
                .map(RoomMapper::toDTO)
                .toList();
    }
}
