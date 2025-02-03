package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.DeviceDTO;
import com.bio.bio_backend.model.Device;

import java.util.List;

/**
 * Klasa do mapowania obiektów typu {@link Device} na ich odpowiedniki DTO ({@link DeviceDTO}).
 * Zawiera metody statyczne, które przekształcają obiekty typu {@link Device} oraz kolekcje tych obiektów na obiekty DTO.
 */
public final class DeviceMapper {

    private DeviceMapper() {
    }

    public static DeviceDTO toDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getMacAddress(),
                device.getScannerSerialNumber()
        );
    }

    public static List<DeviceDTO> toDTOS(List<Device> devices) {
        return devices.stream()
                .map(DeviceMapper::toDTO)
                .toList();
    }
}
