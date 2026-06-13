package com.rick.supertrading.web.dto;

import com.rick.supertrading.domain.service.dto.UpdateScheduleCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

/** All fields optional; a null field leaves that property unchanged. */
public record UpdateScheduleRequest(
        @Min(60) Integer intervalSeconds,
        String actionStrategy,
        @PositiveOrZero Integer waitBeforeLogoutMs,
        Boolean enabled
) {
    public UpdateScheduleCommand toCommand() {
        return new UpdateScheduleCommand(intervalSeconds, actionStrategy, waitBeforeLogoutMs, enabled);
    }
}
