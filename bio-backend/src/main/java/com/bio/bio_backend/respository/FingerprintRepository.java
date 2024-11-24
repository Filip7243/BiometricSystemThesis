package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Fingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FingerprintRepository extends JpaRepository<Fingerprint, Long> {
}
