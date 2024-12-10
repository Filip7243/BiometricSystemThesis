package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends CrudRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.id NOT IN (SELECT 1 FROM User u JOIN u.rooms r2 WHERE u.id = ?1 AND r2.id = r.id)")
    List<Room> findAllRoomsNotAssignedToUser(Long userId);
}
