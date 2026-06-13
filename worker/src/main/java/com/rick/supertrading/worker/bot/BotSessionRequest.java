package com.rick.supertrading.worker.bot;

import com.rick.supertrading.domain.choice.Choice;

import java.util.Map;

/**
 * Inputs for one browser session: where to log in, how to operate the page, and
 * which button the strategy decided to click.
 */
public record BotSessionRequest(
        String loginUrl,
        Map<String, String> selectors,
        String username,
        String password,
        Choice choice,
        long navigateMillis,
        long waitBeforeLogoutMs
) {
}
