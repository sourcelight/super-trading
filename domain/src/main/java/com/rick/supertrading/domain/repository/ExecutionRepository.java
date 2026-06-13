package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.model.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

    /** Idempotency guard: the worker checks this before creating a run. */
    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Execution> findByIdempotencyKey(String idempotencyKey);

    List<Execution> findByScheduleIdAndStartedAtBetweenOrderByStartedAtDesc(
            Long scheduleId, Instant from, Instant to);

    /** Ownership-checked fetch through schedule → credential → owner. */
    Optional<Execution> findByIdAndScheduleCredentialOwnerId(Long id, Long ownerId);
}
