package com.rick.supertrading.domain.service.dto;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.domain.model.Action;

import java.time.Instant;

/** Read model for a recorded GREEN/RED {@link Action}. */
public record ActionView(
        Long id,
        Long executionId,
        Instant actionTime,
        Choice choice,
        String pageUrl,
        Integer durationMs
) {
    public static ActionView from(Action a) {
        return new ActionView(
                a.getId(),
                a.getExecution().getId(),
                a.getActionTime(),
                a.getChoice(),
                a.getPageUrl(),
                a.getDurationMs());
    }
}
