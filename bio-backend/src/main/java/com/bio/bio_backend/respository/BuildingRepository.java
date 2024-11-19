package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Building;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends CrudRepository<Building, Long> {
}
