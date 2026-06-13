package com.rick.supertrading.domain.port;

/**
 * Outbound port for reading an external-site password back from secure storage,
 * given the reference persisted on a credential. Used by the bot worker at run
 * time. Production binds this to AWS Secrets Manager.
 *
 * <p>Counterpart to {@link SecretStore} (write side); kept separate so neither the
 * API nor the worker carries capabilities it doesn't need.
 */
public interface SecretReader {

    /**
     * @param secretRef the reference stored on the credential (e.g. a Secrets
     *                  Manager ARN)
     * @return the plaintext secret value
     */
    String read(String secretRef);
}
