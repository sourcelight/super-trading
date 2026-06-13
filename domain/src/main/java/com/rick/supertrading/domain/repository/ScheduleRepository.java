package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /** Schedules reachable by a user, via the owning credential. */
    List<Schedule> findByCredentialOwnerId(Long ownerId);

    /** Ownership-checked fetch through the credential chain. */
    Optional<Schedule> findByIdAndCredentialOwnerId(Long id, Long ownerId);

    List<Schedule> findByCredentialId(Long credentialId);
}
