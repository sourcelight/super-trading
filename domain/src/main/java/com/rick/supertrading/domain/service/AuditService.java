package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.model.AuditLog;
import com.rick.supertrading.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Writes the append-only audit trail. Called by the mutating services on every
 * create/update/delete/enable/disable of credentials and schedules (spec §9).
 */
@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(Long actorUserId, String entityType, Long entityId, String action,
                       Map<String, Object> details) {
        repository.save(new AuditLog(actorUserId, entityType, entityId, action, details));
    }
}
