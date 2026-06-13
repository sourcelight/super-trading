package com.rick.supertrading.worker.aws;

import com.rick.supertrading.domain.port.SecretReader;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * Reads external-site passwords from AWS Secrets Manager using the ARN persisted
 * on the credential. The plaintext is returned to the caller and never logged.
 */
@Component
public class SecretsManagerSecretReader implements SecretReader {

    private final SecretsManagerClient client;

    public SecretsManagerSecretReader(SecretsManagerClient client) {
        this.client = client;
    }

    @Override
    public String read(String secretRef) {
        return client.getSecretValue(b -> b.secretId(secretRef)).secretString();
    }
}
