package com.rick.supertrading.worker.bot;

/**
 * Outcome of a successful session: the page URL at click time and how long the
 * click took, both recorded on the {@code action} row.
 */
public record BotResult(String pageUrl, int clickDurationMs) {
}
