package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {

    Boolean existsByDeviceHardwareId(String deviceHardwareId);
    Optional<Device> findByDeviceHardwareId(String deviceHardwareId);
}
