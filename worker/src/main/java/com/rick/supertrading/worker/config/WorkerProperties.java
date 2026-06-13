package com.rick.supertrading.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Worker configuration bound from {@code supertrading.worker.*}.
 */
@ConfigurationProperties(prefix = "supertrading.worker")
public class WorkerProperties {

    /** AWS region for SQS/Secrets Manager/S3 clients. */
    private String region;

    /** URL of the SQS queue to consume jobs from. */
    private String queueUrl;

    /** S3 bucket for failure screenshots. */
    private String screenshotBucket;

    /**
     * How long to browse the site before clicking (spec: "navigates for a
     * configurable period"). Not part of the schedule schema, so configured here.
     */
    private long navigateMillis = 2000;

    /** SQS long-poll wait time, in seconds. */
    private int waitTimeSeconds = 20;

    /** Max messages to pull per receive call. */
    private int maxMessages = 10;

    /** Optional SQS endpoint override (local profile → ElasticMQ). Null in aws. */
    private String sqsEndpoint;

    /** Optional S3 endpoint override (local profile → LocalStack/MinIO). Null in aws. */
    private String s3Endpoint;

    /** Optional Secrets Manager endpoint override (local profile → LocalStack). Null in aws. */
    private String secretsEndpoint;

    /** Static access key for local emulators; null → SDK default credential chain. */
    private String accessKey;

    /** Static secret key for local emulators; null → SDK default credential chain. */
    private String secretKey;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public String getScreenshotBucket() {
        return screenshotBucket;
    }

    public void setScreenshotBucket(String screenshotBucket) {
        this.screenshotBucket = screenshotBucket;
    }

    public long getNavigateMillis() {
        return navigateMillis;
    }

    public void setNavigateMillis(long navigateMillis) {
        this.navigateMillis = navigateMillis;
    }

    public int getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public void setWaitTimeSeconds(int waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public String getSqsEndpoint() {
        return sqsEndpoint;
    }

    public void setSqsEndpoint(String sqsEndpoint) {
        this.sqsEndpoint = sqsEndpoint;
    }

    public String getS3Endpoint() {
        return s3Endpoint;
    }

    public void setS3Endpoint(String s3Endpoint) {
        this.s3Endpoint = s3Endpoint;
    }

    public String getSecretsEndpoint() {
        return secretsEndpoint;
    }

    public void setSecretsEndpoint(String secretsEndpoint) {
        this.secretsEndpoint = secretsEndpoint;
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
