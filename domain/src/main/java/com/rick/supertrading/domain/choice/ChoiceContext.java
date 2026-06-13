package com.rick.supertrading.domain.choice;

import java.util.Map;

/**
 * Inputs available to a {@link ChoiceStrategy} when deciding GREEN vs RED.
 *
 * @param scheduleId  the schedule the run belongs to
 * @param pageSignals arbitrary signals scraped from the page (empty for the
 *                    random strategy; used by future signal-driven strategies)
 */
public record ChoiceContext(Long scheduleId, Map<String, Object> pageSignals) {

    public ChoiceContext {
        pageSignals = pageSignals != null ? Map.copyOf(pageSignals) : Map.of();
    }

    public static ChoiceContext forSchedule(Long scheduleId) {
        return new ChoiceContext(scheduleId, Map.of());
    }
}
