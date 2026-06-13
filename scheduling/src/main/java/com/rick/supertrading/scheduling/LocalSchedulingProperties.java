package com.rick.supertrading.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the in-JVM {@link LocalScheduleTrigger} (local profile), bound from
 * {@code supertrading.local-scheduling.*}. It enqueues job messages to an SQS-compatible
 * endpoint (ElasticMQ) — no EventBridge, no 1-minute floor.
 */
@ConfigurationProperties(prefix = "supertrading.local-scheduling")
public class LocalSchedulingProperties {

    /** SQS-compatible endpoint (ElasticMQ), e.g. http://localhost:9324. */
    private String sqsEndpoint;

    /** URL of the jobs queue to enqueue onto. */
    private String queueUrl;

    /** Region for the SQS client (any value accepted by the emulator). */
    private String region = "us-east-1";

    private String accessKey = "local";

    private String secretKey = "local";

    public String getSqsEndpoint() {
        return sqsEndpoint;
    }

    public void setSqsEndpoint(String sqsEndpoint) {
        this.sqsEndpoint = sqsEndpoint;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
