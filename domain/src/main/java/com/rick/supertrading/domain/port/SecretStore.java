package com.rick.supertrading.domain.port;

/**
 * Outbound port for storing external-site passwords outside the database.
 *
 * <p>Production binds this to AWS Secrets Manager; the only thing persisted in our
 * DB is the returned reference (an ARN). The password value never touches our
 * tables or logs.
 */
public interface SecretStore {

    /**
     * Store a secret and return an opaque reference to it.
     *
     * @param name        a human-meaningful name/path for the secret
     * @param secretValue the password to store (never persisted by us)
     * @return the reference (e.g. a Secrets Manager ARN) to persist on the credential
     */
    String store(String name, String secretValue);
}
