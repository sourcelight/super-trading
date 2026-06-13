package com.rick.supertrading.worker.bot;

/**
 * Drives one external-site session: log in, navigate, click the chosen button,
 * wait, log out. Abstracted from the browser engine so the orchestration in
 * {@code BotJobProcessor} can be tested without launching a browser.
 */
public interface BotRunner {

    BotResult runSession(BotSessionRequest request) throws BotExecutionException;
}
