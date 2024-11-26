package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Building;
import com.bio.bio_backend.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    @Query("SELECT r FROM Room r " +
            "WHERE r.building.id IS NOT NULL AND r NOT IN (" +
            "    SELECT ur FROM User u " +
            "    JOIN u.rooms ur " +
            "    WHERE u.id = :userId" +
            ")")
    List<Room> findAllRoomsNotAssignedToUser(@Param("userId") Long userId);
}
