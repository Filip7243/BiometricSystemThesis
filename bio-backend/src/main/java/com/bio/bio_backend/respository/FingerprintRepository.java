package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.FingerType;
import com.bio.bio_backend.model.Fingerprint;
import com.bio.bio_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FingerprintRepository extends JpaRepository<Fingerprint, Long> {

    List<Fingerprint> findByFingerType(FingerType fingerType);

    Optional<Fingerprint> findByFingerTypeAndUser(FingerType fingerType, User user);

    List<Fingerprint> findByUserId(Long userId);
}
