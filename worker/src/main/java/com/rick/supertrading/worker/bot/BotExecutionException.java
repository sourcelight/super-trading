package com.rick.supertrading.worker.bot;

/**
 * Signals that a bot session failed. Carries an optional screenshot (PNG bytes)
 * captured at the point of failure, to be uploaded by the processor (spec §8.1).
 */
public class BotExecutionException extends Exception {

    private final transient byte[] screenshot;

    public BotExecutionException(String message, Throwable cause, byte[] screenshot) {
        super(message, cause);
        this.screenshot = screenshot;
    }

    /** @return the failure screenshot PNG bytes, or null if none could be captured */
    public byte[] getScreenshot() {
        return screenshot;
    }
}
