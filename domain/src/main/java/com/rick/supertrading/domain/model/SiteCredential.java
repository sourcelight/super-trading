package com.rick.supertrading.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * The bot's login to an external website, owned directly by an {@link AppUser}.
 *
 * <p>This is the join between the owning user and a shared {@link Site}: one user
 * may hold many credentials, those credentials may target many (possibly shared)
 * sites, and a user may hold several distinct logins on the same site (enforced by
 * the {@code (owner, site, username)} unique constraint).
 *
 * <p>The external password is <strong>never</strong> stored here — only
 * {@link #secretRef}, an AWS Secrets Manager ARN.
 */
@Entity
@Table(
        name = "site_credential",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_credential_owner_site_username",
                columnNames = {"owner_user_id", "site_id", "username"}
        )
)
public class SiteCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "label")
    private String label;

    @Column(nullable = false)
    private String username;

    /** AWS Secrets Manager ARN. NEVER the password itself. */
    @Column(name = "secret_ref", nullable = false)
    private String secretRef;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SiteCredential() {
        // for JPA
    }

    public SiteCredential(AppUser owner, Site site, String label, String username, String secretRef) {
        this.owner = owner;
        this.site = site;
        this.label = label;
        this.username = username;
        this.secretRef = secretRef;
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public Site getSite() {
        return site;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecretRef() {
        return secretRef;
    }

    public void setSecretRef(String secretRef) {
        this.secretRef = secretRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
