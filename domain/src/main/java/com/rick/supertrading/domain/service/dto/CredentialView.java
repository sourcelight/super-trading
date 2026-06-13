package com.rick.supertrading.domain.service.dto;

import com.rick.supertrading.domain.model.SiteCredential;

import java.time.Instant;

/**
 * Read model for a {@link SiteCredential}. Exposes the secret <em>reference</em>
 * only — never a password.
 */
public record CredentialView(
        Long id,
        Long ownerUserId,
        Long siteId,
        String siteName,
        String label,
        String username,
        String secretRef,
        Instant createdAt
) {
    public static CredentialView from(SiteCredential c) {
        return new CredentialView(
                c.getId(),
                c.getOwner().getId(),
                c.getSite().getId(),
                c.getSite().getName(),
                c.getLabel(),
                c.getUsername(),
                c.getSecretRef(),
                c.getCreatedAt());
    }
}
