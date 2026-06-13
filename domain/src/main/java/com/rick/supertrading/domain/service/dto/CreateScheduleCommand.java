package com.rick.supertrading.domain.service.dto;

/** Inputs to create a schedule against an owned credential. */
public record CreateScheduleCommand(
        Long credentialId,
        int intervalSeconds,
        String actionStrategy,
        int waitBeforeLogoutMs
) {
}
