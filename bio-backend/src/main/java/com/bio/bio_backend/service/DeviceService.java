package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.DeviceDTO;
import com.bio.bio_backend.respository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bio.bio_backend.mapper.DeviceMapper.toDTOS;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public List<DeviceDTO> getDevicesNotAssignedToRoom() {
        return toDTOS(deviceRepository.findByRoomIsNull());
    }
}
