package com.rick.supertrading.worker.aws;

import com.rick.supertrading.domain.port.ScreenshotStore;
import com.rick.supertrading.worker.config.WorkerProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Instant;

/**
 * Uploads failure screenshots to S3 and returns the object key to persist on the
 * execution (spec §8.1).
 */
@Component
public class S3ScreenshotStore implements ScreenshotStore {

    private final S3Client client;
    private final WorkerProperties properties;

    public S3ScreenshotStore(S3Client client, WorkerProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public String store(Long executionId, byte[] pngBytes) {
        String key = "screenshots/%d/%d.png".formatted(executionId, Instant.now().toEpochMilli());
        client.putObject(
                b -> b.bucket(properties.getScreenshotBucket()).key(key).contentType("image/png"),
                RequestBody.fromBytes(pngBytes));
        return key;
    }
}
