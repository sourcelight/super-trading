package com.rick.supertrading.domain.service.dto;

import com.rick.supertrading.domain.model.Execution;
import com.rick.supertrading.domain.model.ExecutionStatus;

import java.time.Instant;

/** Read model for an {@link Execution}; also the payload pushed to live monitoring. */
public record ExecutionView(
        Long id,
        Long scheduleId,
        ExecutionStatus status,
        Instant startedAt,
        Instant endedAt,
        String errorMessage,
        String screenshotS3Key
) {
    public static ExecutionView from(Execution e) {
        return new ExecutionView(
                e.getId(),
                e.getSchedule().getId(),
                e.getStatus(),
                e.getStartedAt(),
                e.getEndedAt(),
                e.getErrorMessage(),
                e.getScreenshotS3Key());
    }
}
