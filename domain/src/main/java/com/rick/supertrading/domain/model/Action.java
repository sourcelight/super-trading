package com.rick.supertrading.domain.model;

import com.rick.supertrading.domain.choice.Choice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A single GREEN/RED button click recorded during an {@link Execution}.
 */
@Entity
@Table(name = "action")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "execution_id", nullable = false)
    private Execution execution;

    @CreationTimestamp
    @Column(name = "action_time", nullable = false, updatable = false)
    private Instant actionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Choice choice;

    @Column(name = "page_url")
    private String pageUrl;

    @Column(name = "duration_ms")
    private Integer durationMs;

    protected Action() {
        // for JPA
    }

    public Action(Execution execution, Choice choice, String pageUrl, Integer durationMs) {
        this.execution = execution;
        this.choice = choice;
        this.pageUrl = pageUrl;
        this.durationMs = durationMs;
    }

    public Long getId() {
        return id;
    }

    public Execution getExecution() {
        return execution;
    }

    public Instant getActionTime() {
        return actionTime;
    }

    public Choice getChoice() {
        return choice;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public Integer getDurationMs() {
        return durationMs;
    }
}
