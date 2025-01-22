package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.DeviceDTO;
import com.bio.bio_backend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kontroler REST do zarządzania urządzeniami.
 * Umożliwia operacje związane z urządzeniami, w tym pobieranie urządzeń, które nie zostały przypisane do żadnego pokoju.
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * Pobiera listę urządzeń, które nie zostały przypisane do żadnego pokoju.
     *
     * @return Lista urządzeń, które nie są przypisane do pokoju.
     */
    @GetMapping("/not-assigned")
    public List<DeviceDTO> getDevicesNotAssignedToRoom() {
        return deviceService.getDevicesNotAssignedToRoom();
    }
}
