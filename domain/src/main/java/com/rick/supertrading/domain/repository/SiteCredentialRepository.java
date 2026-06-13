package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.model.SiteCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SiteCredentialRepository extends JpaRepository<SiteCredential, Long> {

    /** All credentials owned by a given console user (USER-scoped listing). */
    List<SiteCredential> findByOwnerId(Long ownerId);

    /** Ownership-checked fetch: returns empty if the credential is not owned by the user. */
    Optional<SiteCredential> findByIdAndOwnerId(Long id, Long ownerId);
}
