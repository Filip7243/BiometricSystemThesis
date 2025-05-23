package com.bio.bio_backend.respository;

import com.bio.bio_backend.dto.LateControlDTO;
import com.bio.bio_backend.dto.RoomEntranceDTO;
import com.bio.bio_backend.dto.UnconfirmedEntranceDTO;
import com.bio.bio_backend.dto.UserEnrollmentConfirmationDTO;
import com.bio.bio_backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT new com.bio.bio_backend.dto.RoomEntranceDTO(" +
            "e.room.roomNumber, " +
            "b.buildingNumber, " +
            "COUNT(e)) " +
            "FROM Enrollment e " +
            "JOIN e.room r " +
            "JOIN r.building b " +
            "WHERE FUNCTION('DATE', e.enrollmentDate) = :date " +
            "AND b.id = :buildingId " +
            "GROUP BY e.room.roomNumber, b.buildingNumber")
    List<RoomEntranceDTO> getNumberOfEntrancesToEachRoomOnDate(
            @Param("date") LocalDate date,
            @Param("buildingId") Long buildingId);

    @Query("SELECT new com.bio.bio_backend.dto.UnconfirmedEntranceDTO(" +
            "e.user.firstName, e.user.lastName, e.room.roomNumber, e.room.building.buildingNumber, COUNT(e)) " +
            "FROM Enrollment e " +
            "WHERE e.isConfirmed = false " +
            "AND e.room NOT IN (SELECT r FROM User u JOIN u.rooms r WHERE u.id = e.user.id) " +
            "GROUP BY e.user.firstName, e.user.lastName, e.room.roomNumber, e.room.building.buildingNumber")
    List<UnconfirmedEntranceDTO> getUnconfirmedEntrancesPerUserByRoom();

    @Query("SELECT new com.bio.bio_backend.dto.UserEnrollmentConfirmationDTO(" +
            "e.user.firstName, e.isConfirmed, e.fingerprint.fingerType, COUNT(e)) " +
            "FROM Enrollment e " +
            "WHERE e.user.id = :userId " +
            "AND e.isConfirmed = false " +
            "AND e.room IN (SELECT r FROM User u JOIN u.rooms r WHERE u.id = :userId) " +
            "GROUP BY e.user.firstName, e.isConfirmed, e.fingerprint.fingerType")
    List<UserEnrollmentConfirmationDTO> getUserEnrollmentConfirmationRate(@Param("userId") Long userId);

    @Query("SELECT new com.bio.bio_backend.dto.LateControlDTO(" +
            "e.user.firstName, e.user.lastName, e.room.roomNumber, e.room.building.buildingNumber, e.enrollmentDate) " +
            "FROM Enrollment e " +
            "WHERE FUNCTION('DATE', e.enrollmentDate) = :date " +
            "AND e.user.id = :userId " +
            "AND HOUR(e.enrollmentDate) > :expectedHour")
    List<LateControlDTO> getLateControlByUserAndRoom(
            @Param("date") LocalDate date,
            @Param("userId") Long userId,
            @Param("expectedHour") int expectedHour);
}
