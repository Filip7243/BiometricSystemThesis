package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.model.Room;

import java.util.List;
import java.util.Set;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomDTO toDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getDevice() != null ? room.getDevice().getDeviceHardwareId() : null
        );
    }

    public static List<RoomDTO> toDTOS(Set<Room> rooms) {
        return rooms.stream()
                .map(r -> new RoomDTO(
                        r.getId(),
                        r.getRoomNumber(),
                        r.getFloor(),
                        r.getDevice().getDeviceHardwareId()))
                .toList();
    }
}
