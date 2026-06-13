package com.rick.supertrading.config;

import com.rick.supertrading.domain.port.SecretStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.ResourceExistsException;

import java.net.URI;

/**
 * Local-profile {@link SecretStore} backed by a Secrets Manager-compatible endpoint
 * (LocalStack). Because the worker reads through the same API (its
 * {@code SecretsManagerSecretReader}), credentials created in the console are readable by
 * the bot end to end — no special casing. Replaces the no-op local store under {@code local}.
 */
@Configuration
@Profile("local")
public class LocalSecretsConfig {

    @Bean
    public SecretsManagerClient localSecretsManagerClient(
            @Value("${supertrading.local-secrets.endpoint:http://localhost:4566}") String endpoint,
            @Value("${supertrading.local-secrets.region:us-east-1}") String region,
            @Value("${supertrading.local-secrets.access-key:local}") String accessKey,
            @Value("${supertrading.local-secrets.secret-key:local}") String secretKey) {
        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean
    public SecretStore secretStore(SecretsManagerClient localSecretsManagerClient) {
        return (name, secretValue) -> {
            try {
                localSecretsManagerClient.createSecret(b -> b.name(name).secretString(secretValue));
            } catch (ResourceExistsException alreadyExists) {
                localSecretsManagerClient.putSecretValue(b -> b.secretId(name).secretString(secretValue));
            }
            // The worker resolves the value via SecretReader.read(<name>).
            return name;
        };
    }
}
