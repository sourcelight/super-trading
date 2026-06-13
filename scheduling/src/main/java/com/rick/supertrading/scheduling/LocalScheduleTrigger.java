package com.rick.supertrading.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.port.ScheduleProvisioner;
import com.rick.supertrading.domain.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Local profile implementation of {@link ScheduleProvisioner}: an in-JVM
 * {@link TaskScheduler} that, every {@code intervalSeconds}, enqueues the <em>same</em>
 * SQS-shaped message the production EventBridge target produces
 * ({@code {"scheduleId":..,"idempotencyKey":..}}) onto an SQS-compatible queue (ElasticMQ).
 * The worker's consumer is therefore unchanged across environments. Local scheduling has no
 * 1-minute floor, so demos can use short intervals (e.g. 30s).
 *
 * <p>Runs inside the API JVM (where {@code ScheduleService} invokes the provisioner). On
 * startup it re-registers all enabled schedules from the database so a restart resumes firing.
 */
public class LocalScheduleTrigger implements ScheduleProvisioner {

    private static final Logger log = LoggerFactory.getLogger(LocalScheduleTrigger.class);
    private static final String REF_PREFIX = "local-schedule://";

    private final TaskScheduler taskScheduler;
    private final SqsClient sqs;
    private final ObjectMapper objectMapper;
    private final ScheduleRepository schedules;
    private final LocalSchedulingProperties properties;

    private final Map<Long, ScheduledFuture<?>> running = new ConcurrentHashMap<>();

    public LocalScheduleTrigger(TaskScheduler taskScheduler,
                                SqsClient sqs,
                                ObjectMapper objectMapper,
                                ScheduleRepository schedules,
                                LocalSchedulingProperties properties) {
        this.taskScheduler = taskScheduler;
        this.sqs = sqs;
        this.objectMapper = objectMapper;
        this.schedules = schedules;
        this.properties = properties;
    }

    @Override
    public String create(Schedule schedule) {
        register(schedule);
        return REF_PREFIX + schedule.getId();
    }

    @Override
    public void update(Schedule schedule) {
        cancel(schedule.getId());
        register(schedule);
    }

    @Override
    public void delete(String scheduleArn) {
        if (scheduleArn == null) {
            return;
        }
        Long id = idFromRef(scheduleArn);
        if (id != null) {
            cancel(id);
        }
    }

    /** Re-register enabled schedules after the context (and Flyway/seed) are ready. */
    @EventListener(ApplicationReadyEvent.class)
    @Order(20)
    public void registerExisting() {
        for (Schedule schedule : schedules.findAll()) {
            if (schedule.isEnabled()) {
                register(schedule);
            }
        }
        log.info("LocalScheduleTrigger registered {} enabled schedule(s) on startup", running.size());
    }

    private void register(Schedule schedule) {
        if (!schedule.isEnabled()) {
            return;
        }
        Long id = schedule.getId();
        Duration interval = Duration.ofSeconds(schedule.getIntervalSeconds());
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(() -> fire(id), interval);
        ScheduledFuture<?> previous = running.put(id, future);
        if (previous != null) {
            previous.cancel(false);
        }
        log.info("Registered local schedule {} every {}s", id, schedule.getIntervalSeconds());
    }

    private void cancel(Long scheduleId) {
        ScheduledFuture<?> future = running.remove(scheduleId);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled local schedule {}", scheduleId);
        }
    }

    private void fire(Long scheduleId) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("scheduleId", scheduleId);
            body.put("idempotencyKey", UUID.randomUUID().toString());
            String json = objectMapper.writeValueAsString(body);
            sqs.sendMessage(b -> b.queueUrl(properties.getQueueUrl()).messageBody(json));
            log.debug("Enqueued local job for schedule {}", scheduleId);
        } catch (Exception ex) {
            log.error("Failed to enqueue local job for schedule {}: {}", scheduleId, ex.getMessage(), ex);
        }
    }

    private Long idFromRef(String ref) {
        try {
            return Long.parseLong(ref.startsWith(REF_PREFIX) ? ref.substring(REF_PREFIX.length()) : ref);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
