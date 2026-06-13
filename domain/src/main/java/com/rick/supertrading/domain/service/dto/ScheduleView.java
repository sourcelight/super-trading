package com.rick.supertrading.domain.service.dto;

import com.rick.supertrading.domain.model.Schedule;

import java.time.Instant;

/** Read model for a {@link Schedule}. */
public record ScheduleView(
        Long id,
        Long credentialId,
        int intervalSeconds,
        String actionStrategy,
        int waitBeforeLogoutMs,
        boolean enabled,
        String eventbridgeScheduleArn,
        Instant createdAt
) {
    public static ScheduleView from(Schedule s) {
        return new ScheduleView(
                s.getId(),
                s.getCredential().getId(),
                s.getIntervalSeconds(),
                s.getActionStrategy(),
                s.getWaitBeforeLogoutMs(),
                s.isEnabled(),
                s.getEventbridgeScheduleArn(),
                s.getCreatedAt());
    }
}
