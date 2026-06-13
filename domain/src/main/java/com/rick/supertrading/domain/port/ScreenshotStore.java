package com.rick.supertrading.domain.port;

/**
 * Outbound port for persisting a failure screenshot. Production binds this to S3;
 * the returned key is stored on the failed {@code execution} (spec §3, §8.1).
 */
public interface ScreenshotStore {

    /**
     * @param executionId the failed execution the screenshot belongs to
     * @param pngBytes    the captured PNG image
     * @return the storage key (e.g. S3 object key) to persist on the execution
     */
    String store(Long executionId, byte[] pngBytes);
}
