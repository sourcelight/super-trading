package com.rick.supertrading.domain.service.dto;

/**
 * Inputs to create a bot credential. The {@code password} is handed to the
 * {@link com.rick.supertrading.domain.port.SecretStore} and never persisted by us.
 */
public record CreateCredentialCommand(
        Long siteId,
        String label,
        String username,
        String password
) {
}
