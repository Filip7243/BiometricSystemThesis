package com.bio.bio_backend.respository;

import com.bio.bio_backend.model.Fingerprint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintRepository extends CrudRepository<Fingerprint, Long> {
}
