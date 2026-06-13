package com.rick.supertrading.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A person who logs into the admin console. Authenticated via Cognito (OIDC);
 * {@link #externalIdpSub} links the JWT {@code sub} claim to this row.
 *
 * <p>This is the ownership root of the data model: a user owns many
 * {@link SiteCredential}s, which in turn own schedules, executions and actions.
 * Not to be confused with {@link SiteCredential}, which is the bot's login to an
 * <em>external</em> website.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "external_idp_sub", unique = true)
    private String externalIdpSub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AppUser() {
        // for JPA
    }

    public AppUser(String email, String displayName, String externalIdpSub, Role role) {
        this.email = email;
        this.displayName = displayName;
        this.externalIdpSub = externalIdpSub;
        this.role = role != null ? role : Role.USER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExternalIdpSub() {
        return externalIdpSub;
    }

    public void setExternalIdpSub(String externalIdpSub) {
        this.externalIdpSub = externalIdpSub;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
