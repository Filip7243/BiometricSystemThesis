package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.DeviceDTO;
import com.bio.bio_backend.respository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bio.bio_backend.mapper.DeviceMapper.toDTOS;

/**
 * Serwis zarządzający operacjami na urządzeniach.
 * <p>
 * Klasa odpowiada za obsługę operacji związanych z urządzeniami, takich jak pobieranie
 * urządzeń nieprzypisanych do żadnego pokoju.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    /**
     * Pobiera listę urządzeń, które nie są przypisane do żadnego pokoju.
     *
     * @return Lista obiektów DTO urządzeń nieprzypisanych do pokoju.
     */
    public List<DeviceDTO> getDevicesNotAssignedToRoom() {
        return toDTOS(deviceRepository.findByRoomIsNull());
    }
}
