package com.rick.supertrading.web.dto;

import com.rick.supertrading.domain.service.dto.CreateScheduleCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateScheduleRequest(
        @NotNull Long credentialId,
        // EventBridge granularity floor is 60s (spec §13); sub-minute is out of scope for v1.
        @Min(60) int intervalSeconds,
        String actionStrategy,
        @PositiveOrZero int waitBeforeLogoutMs
) {
    public CreateScheduleCommand toCommand() {
        String strategy = (actionStrategy == null || actionStrategy.isBlank()) ? "random" : actionStrategy;
        return new CreateScheduleCommand(credentialId, intervalSeconds, strategy, waitBeforeLogoutMs);
    }
}
