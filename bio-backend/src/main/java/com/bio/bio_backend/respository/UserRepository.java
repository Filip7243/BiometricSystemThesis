package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Role;
import com.bio.bio_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR :search IS NULL OR :search = '') " +
            "OR (LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR :search IS NULL OR :search = '') " +
            "OR (LOWER(u.pesel) LIKE LOWER(CONCAT('%', :search, '%')) OR :search IS NULL OR :search = '')")
    List<User> searchByFields(@Param("search") String search);

    Boolean existsByIdAndRole(Long id, Role role);
}
