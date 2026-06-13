package com.rick.supertrading.domain.service.dto;

import java.util.Map;

/**
 * Everything the bot worker needs to execute one run, flattened from the
 * schedule → credential → site chain inside a transaction so the worker never
 * touches lazy associations.
 *
 * @param executionId       the freshly created RUNNING execution
 * @param scheduleId        owning schedule (for the choice context)
 * @param loginUrl          site login URL
 * @param selectors         site CSS selectors (username/password/green/red/logout)
 * @param username          the bot's login name
 * @param secretRef         reference used to read the password from the SecretReader
 * @param actionStrategy    strategy key resolved via ChoiceStrategyResolver
 * @param waitBeforeLogoutMs pause between clicking and logging out
 */
public record BotJob(
        Long executionId,
        Long scheduleId,
        String loginUrl,
        Map<String, String> selectors,
        String username,
        String secretRef,
        String actionStrategy,
        int waitBeforeLogoutMs
) {
}
