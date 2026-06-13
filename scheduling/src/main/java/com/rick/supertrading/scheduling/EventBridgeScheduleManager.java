package com.rick.supertrading.scheduling;

import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.port.ScheduleProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindow;
import software.amazon.awssdk.services.scheduler.model.FlexibleTimeWindowMode;
import software.amazon.awssdk.services.scheduler.model.ResourceNotFoundException;
import software.amazon.awssdk.services.scheduler.model.ScheduleState;
import software.amazon.awssdk.services.scheduler.model.Target;

/**
 * Provisions one EventBridge schedule per domain {@link Schedule}. On fire the
 * schedule delivers a message to the configured SQS queue; the bot worker consumes
 * it (Step 5). This is the durable, external timer the spec requires (§4) — not a
 * JVM-local scheduler.
 */
public class EventBridgeScheduleManager implements ScheduleProvisioner {

    private static final Logger log = LoggerFactory.getLogger(EventBridgeScheduleManager.class);

    private final SchedulerClient client;
    private final SchedulingProperties properties;

    public EventBridgeScheduleManager(SchedulerClient client, SchedulingProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public String create(Schedule schedule) {
        String name = ScheduleExpressions.scheduleName(schedule.getId());
        String arn = client.createSchedule(b -> b
                .name(name)
                .groupName(properties.getGroupName())
                .scheduleExpression(ScheduleExpressions.rateExpression(schedule.getIntervalSeconds()))
                .flexibleTimeWindow(FlexibleTimeWindow.builder()
                        .mode(FlexibleTimeWindowMode.OFF).build())
                .state(stateOf(schedule))
                .target(target(schedule))
        ).scheduleArn();
        log.info("Created EventBridge schedule {} ({})", name, arn);
        return arn;
    }

    @Override
    public void update(Schedule schedule) {
        String name = ScheduleExpressions.scheduleName(schedule.getId());
        client.updateSchedule(b -> b
                .name(name)
                .groupName(properties.getGroupName())
                .scheduleExpression(ScheduleExpressions.rateExpression(schedule.getIntervalSeconds()))
                .flexibleTimeWindow(FlexibleTimeWindow.builder()
                        .mode(FlexibleTimeWindowMode.OFF).build())
                .state(stateOf(schedule))
                .target(target(schedule)));
        log.info("Updated EventBridge schedule {} (enabled={})", name, schedule.isEnabled());
    }

    @Override
    public void delete(String scheduleArn) {
        if (scheduleArn == null) {
            return;
        }
        String name = ScheduleExpressions.nameFromArn(scheduleArn);
        try {
            client.deleteSchedule(b -> b.name(name).groupName(properties.getGroupName()));
            log.info("Deleted EventBridge schedule {}", name);
        } catch (ResourceNotFoundException alreadyGone) {
            log.warn("EventBridge schedule {} already absent; nothing to delete", name);
        }
    }

    private Target target(Schedule schedule) {
        return Target.builder()
                .arn(properties.getQueueArn())
                .roleArn(properties.getSchedulerRoleArn())
                .input(ScheduleExpressions.targetInput(schedule.getId()))
                .build();
    }

    private static ScheduleState stateOf(Schedule schedule) {
        return schedule.isEnabled() ? ScheduleState.ENABLED : ScheduleState.DISABLED;
    }
}
