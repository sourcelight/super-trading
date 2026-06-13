package com.rick.supertrading.scheduling;

/**
 * Pure helpers translating a domain schedule into EventBridge Scheduler concepts.
 * Kept free of the AWS SDK so the mapping logic is unit-testable.
 */
final class ScheduleExpressions {

    private ScheduleExpressions() {
    }

    /**
     * Convert an interval in seconds to an EventBridge {@code rate(...)} expression.
     * EventBridge's floor is 1 minute (spec §13: sub-minute is out of scope for v1),
     * and rate units are whole minutes, so sub-minute and non-minute-multiple
     * intervals are rounded to the nearest minute (minimum 1).
     */
    static String rateExpression(int intervalSeconds) {
        long minutes = Math.max(1, Math.round(intervalSeconds / 60.0));
        return minutes == 1 ? "rate(1 minute)" : "rate(" + minutes + " minutes)";
    }

    /** Deterministic schedule name from the domain schedule id. */
    static String scheduleName(Long scheduleId) {
        return "supertrading-schedule-" + scheduleId;
    }

    /**
     * Target input delivered to SQS on each fire. The reserved context attribute
     * {@code <aws.scheduler.execution-id>} is substituted by EventBridge per
     * invocation, giving the worker a unique idempotency key (spec §8.2).
     */
    static String targetInput(Long scheduleId) {
        return "{\"scheduleId\":" + scheduleId
                + ",\"idempotencyKey\":\"<aws.scheduler.execution-id>\"}";
    }

    /** Extract the schedule name from a full schedule ARN ({@code .../group/name}). */
    static String nameFromArn(String scheduleArn) {
        int slash = scheduleArn.lastIndexOf('/');
        return slash >= 0 ? scheduleArn.substring(slash + 1) : scheduleArn;
    }
}
