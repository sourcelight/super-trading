package com.rick.supertrading.worker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The SQS message body produced by EventBridge on each fire:
 * {@code {"scheduleId":42,"idempotencyKey":"<execution-id>"}} (spec §8.2).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JobMessage(Long scheduleId, String idempotencyKey) {
}
