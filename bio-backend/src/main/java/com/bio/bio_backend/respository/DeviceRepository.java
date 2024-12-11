package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Device;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends CrudRepository<Device, Long> {

    Boolean existsByMacAddress(String macAddress);
    Optional<Device> findByMacAddress(String macAddress);
    List<Device> findByRoomIsNull();
}
