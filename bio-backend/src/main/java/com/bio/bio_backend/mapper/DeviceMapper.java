package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.DeviceDTO;
import com.bio.bio_backend.model.Device;

import java.util.List;

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
