package com.rick.supertrading.domain.service.dto;

import java.time.Instant;

/** GREEN-vs-RED aggregation over a time window, for dashboard charts. */
public record ChoiceStatsView(
        Instant from,
        Instant to,
        long green,
        long red
) {
    public long total() {
        return green + red;
    }
}
