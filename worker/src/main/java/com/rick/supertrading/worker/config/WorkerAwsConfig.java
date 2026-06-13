package com.rick.supertrading.worker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * AWS SDK v2 clients for the worker. Region comes from {@code supertrading.worker.region}
 * when set; otherwise the SDK's default region provider chain applies. Each bean is
 * {@code @ConditionalOnMissingBean} so tests can supply their own.
 *
 * <p>For the {@code local} profile, optional endpoint + static-credential properties point
 * the same clients at AWS-API-compatible emulators (ElasticMQ, LocalStack) — the worker code
 * is otherwise identical across environments. When those properties are null (the {@code aws}
 * profile), the SDK default endpoints and credential chain apply.
 */
@Configuration
@EnableConfigurationProperties(WorkerProperties.class)
public class WorkerAwsConfig {

    private final WorkerProperties properties;

    public WorkerAwsConfig(WorkerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqsClient sqsClient() {
        return applyCommon(SqsClient.builder(), properties.getSqsEndpoint()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretsManagerClient secretsManagerClient() {
        return applyCommon(SecretsManagerClient.builder(), properties.getSecretsEndpoint()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client() {
        S3ClientBuilder builder = applyCommon(S3Client.builder(), properties.getS3Endpoint());
        if (properties.getS3Endpoint() != null) {
            // LocalStack/MinIO require path-style addressing.
            builder.forcePathStyle(true);
        }
        return builder.build();
    }

    private <B extends AwsClientBuilder<B, ?>> B applyCommon(B builder, String endpoint) {
        if (properties.getRegion() != null) {
            builder.region(Region.of(properties.getRegion()));
        }
        if (endpoint != null) {
            builder.endpointOverride(URI.create(endpoint));
        }
        if (properties.getAccessKey() != null && properties.getSecretKey() != null) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        }
        return builder;
    }
}
