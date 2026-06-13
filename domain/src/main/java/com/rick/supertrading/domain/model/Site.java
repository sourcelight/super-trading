package com.rick.supertrading.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * A shared catalog entry describing a target website: its URLs and the CSS
 * selectors the bot uses to operate it. Deliberately <em>not</em> owned by a
 * single user — the same site can be targeted by many users' credentials.
 *
 * <p>{@link #selectors} keys are well-known: {@code username}, {@code password},
 * {@code green}, {@code red}, {@code logout}.
 */
@Entity
@Table(name = "site")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_url", nullable = false, unique = true)
    private String baseUrl;

    @Column(name = "login_url", nullable = false)
    private String loginUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selectors_json", nullable = false)
    private Map<String, String> selectors;

    /** Audit only: the user who first registered this catalog entry. */
    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Site() {
        // for JPA
    }

    public Site(String name, String baseUrl, String loginUrl, Map<String, String> selectors, Long createdBy) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.loginUrl = loginUrl;
        this.selectors = selectors;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public Map<String, String> getSelectors() {
        return selectors;
    }

    public void setSelectors(Map<String, String> selectors) {
        this.selectors = selectors;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
