package com.bio.bio_backend.controller;

import com.bio.bio_backend.dto.DeviceDTO;
import com.bio.bio_backend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/not-assigned")
    public List<DeviceDTO> getDevicesNotAssignedToRoom() {
        return deviceService.getDevicesNotAssignedToRoom();
    }
}
