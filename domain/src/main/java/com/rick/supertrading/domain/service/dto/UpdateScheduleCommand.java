package com.rick.supertrading.domain.service.dto;

/** Mutable fields of a schedule; nulls mean "leave unchanged". */
public record UpdateScheduleCommand(
        Integer intervalSeconds,
        String actionStrategy,
        Integer waitBeforeLogoutMs,
        Boolean enabled
) {
}
