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
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * The "every N seconds" configuration attached to a {@link SiteCredential}.
 * Through the credential it transitively belongs to a user and targets a site.
 *
 * <p>{@link #eventbridgeScheduleArn} links this row to the AWS EventBridge
 * schedule created by the backend; it is null until the schedule is provisioned.
 */
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private SiteCredential credential;

    @Column(name = "interval_seconds", nullable = false)
    private int intervalSeconds;

    /** Strategy key resolved by {@code ChoiceStrategyResolver}; defaults to "random". */
    @Column(name = "action_strategy", nullable = false, length = 50)
    private String actionStrategy = "random";

    @Column(name = "wait_before_logout_ms", nullable = false)
    private int waitBeforeLogoutMs = 3000;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "eventbridge_schedule_arn")
    private String eventbridgeScheduleArn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Schedule() {
        // for JPA
    }

    public Schedule(SiteCredential credential, int intervalSeconds, String actionStrategy, int waitBeforeLogoutMs) {
        this.credential = credential;
        this.intervalSeconds = intervalSeconds;
        this.actionStrategy = actionStrategy != null ? actionStrategy : "random";
        this.waitBeforeLogoutMs = waitBeforeLogoutMs;
    }

    public Long getId() {
        return id;
    }

    public SiteCredential getCredential() {
        return credential;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public String getActionStrategy() {
        return actionStrategy;
    }

    public void setActionStrategy(String actionStrategy) {
        this.actionStrategy = actionStrategy;
    }

    public int getWaitBeforeLogoutMs() {
        return waitBeforeLogoutMs;
    }

    public void setWaitBeforeLogoutMs(int waitBeforeLogoutMs) {
        this.waitBeforeLogoutMs = waitBeforeLogoutMs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEventbridgeScheduleArn() {
        return eventbridgeScheduleArn;
    }

    public void setEventbridgeScheduleArn(String eventbridgeScheduleArn) {
        this.eventbridgeScheduleArn = eventbridgeScheduleArn;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
