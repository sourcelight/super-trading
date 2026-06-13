package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId);
}
