package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** Resolve the console user from the Cognito {@code sub} claim on a JWT. */
    Optional<AppUser> findByExternalIdpSub(String externalIdpSub);

    Optional<AppUser> findByEmail(String email);
}
