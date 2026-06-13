package com.rick.supertrading.scheduling;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleExpressionsTest {

    @Test
    void rateExpressionUsesSingularForOneMinute() {
        assertThat(ScheduleExpressions.rateExpression(60)).isEqualTo("rate(1 minute)");
        assertThat(ScheduleExpressions.rateExpression(300)).isEqualTo("rate(5 minutes)");
    }

    @Test
    void rateExpressionRoundsToNearestMinuteWithAFloorOfOne() {
        assertThat(ScheduleExpressions.rateExpression(30)).isEqualTo("rate(1 minute)");   // sub-minute floored
        assertThat(ScheduleExpressions.rateExpression(90)).isEqualTo("rate(2 minutes)");  // rounded up
        assertThat(ScheduleExpressions.rateExpression(89)).isEqualTo("rate(1 minute)");   // rounded down
    }

    @Test
    void targetInputEmbedsScheduleIdAndExecutionIdPlaceholder() {
        assertThat(ScheduleExpressions.targetInput(42L))
                .isEqualTo("{\"scheduleId\":42,\"idempotencyKey\":\"<aws.scheduler.execution-id>\"}");
    }

    @Test
    void scheduleNameIsDeterministic() {
        assertThat(ScheduleExpressions.scheduleName(7L)).isEqualTo("supertrading-schedule-7");
    }

    @Test
    void nameFromArnExtractsLastSegment() {
        assertThat(ScheduleExpressions.nameFromArn(
                "arn:aws:scheduler:eu-west-1:123456789012:schedule/default/supertrading-schedule-7"))
                .isEqualTo("supertrading-schedule-7");
        assertThat(ScheduleExpressions.nameFromArn("plain-name")).isEqualTo("plain-name");
    }
}
