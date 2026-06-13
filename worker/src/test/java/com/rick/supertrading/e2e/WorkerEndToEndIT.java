package com.rick.supertrading.e2e;

import com.rick.supertrading.domain.WorkerE2ETestConfig;
import com.rick.supertrading.domain.WorkerE2ETestConfig.FakeBotRunner;
import com.rick.supertrading.domain.WorkerE2ETestConfig.FakeScreenshotStore;
import com.rick.supertrading.domain.WorkerE2ETestConfig.RecordingMonitoringPublisher;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.ExecutionStatus;
import com.rick.supertrading.domain.model.Role;
import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.model.Site;
import com.rick.supertrading.domain.model.SiteCredential;
import com.rick.supertrading.domain.repository.ActionRepository;
import com.rick.supertrading.domain.repository.AppUserRepository;
import com.rick.supertrading.domain.repository.ExecutionRepository;
import com.rick.supertrading.domain.repository.ScheduleRepository;
import com.rick.supertrading.domain.repository.SiteCredentialRepository;
import com.rick.supertrading.domain.repository.SiteRepository;
import com.rick.supertrading.worker.BotJobProcessor;
import com.rick.supertrading.worker.JobMessage;
import com.rick.supertrading.worker.bot.BotExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of the bot worker's data path (spec §12 step 8) against a real
 * PostgreSQL database with the Flyway schema applied: a seeded user → credential →
 * schedule is driven through {@link BotJobProcessor} with a stubbed browser, and we
 * assert that the execution and its click are recorded, that a duplicate delivery is
 * skipped (idempotency), and that a bot failure is recorded with a screenshot key.
 *
 * <p>Docker-gated: skips when no daemon is present; runs fully in CI.
 */
@SpringBootTest(classes = WorkerE2ETestConfig.class)
@EnabledIf("dockerAvailable")
class WorkerEndToEndIT {

    static PostgreSQLContainer<?> postgres;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Autowired BotJobProcessor processor;
    @Autowired FakeBotRunner botRunner;
    @Autowired FakeScreenshotStore screenshotStore;
    @Autowired RecordingMonitoringPublisher monitoring;

    @Autowired AppUserRepository users;
    @Autowired SiteRepository sites;
    @Autowired SiteCredentialRepository credentials;
    @Autowired ScheduleRepository schedules;
    @Autowired ExecutionRepository executions;
    @Autowired ActionRepository actions;

    private Long scheduleId;

    @BeforeEach
    void seed() {
        botRunner.failure = null;
        monitoring.published.clear();

        AppUser user = users.save(new AppUser("e2e@example.com", "E2E", "sub-e2e-" + uniqueSuffix(), Role.USER));
        Site site = sites.save(new Site(
                "Mock", "https://mock.local-" + uniqueSuffix(), "https://mock.local/login",
                Map.of("username", "#u", "password", "#p", "green", ".g", "red", ".r", "logout", "#o"),
                user.getId()));
        SiteCredential credential = credentials.save(
                new SiteCredential(user, site, "primary", "bot-user", "arn:test:secret"));
        Schedule schedule = schedules.save(new Schedule(credential, 60, "random", 0));
        scheduleId = schedule.getId();
    }

    @Test
    void recordsExecutionAndActionOnSuccess() {
        BotJobProcessor.Outcome outcome = processor.process(new JobMessage(scheduleId, "idem-success"));

        assertThat(outcome).isEqualTo(BotJobProcessor.Outcome.SUCCESS);
        var execution = executions.findByIdempotencyKey("idem-success").orElseThrow();
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(execution.getEndedAt()).isNotNull();

        var clicks = actions.findByExecutionIdOrderByActionTime(execution.getId());
        assertThat(clicks).hasSize(1);
        assertThat(clicks.get(0).getChoice()).isNotNull();
        assertThat(clicks.get(0).getPageUrl()).isEqualTo("https://mock.local/page");

        // The run status was surfaced for live monitoring.
        assertThat(monitoring.published).extracting(v -> v.status()).contains(ExecutionStatus.SUCCESS);
    }

    @Test
    void skipsDuplicateDeliveryAndDoesNotCreateASecondExecution() {
        processor.process(new JobMessage(scheduleId, "idem-dup"));
        BotJobProcessor.Outcome second = processor.process(new JobMessage(scheduleId, "idem-dup"));

        assertThat(second).isEqualTo(BotJobProcessor.Outcome.DUPLICATE);
        var execution = executions.findByIdempotencyKey("idem-dup").orElseThrow();
        // Exactly one click recorded — the duplicate did not run the bot again.
        assertThat(actions.findByExecutionIdOrderByActionTime(execution.getId())).hasSize(1);
    }

    @Test
    void recordsFailureWithScreenshotOnBotError() {
        botRunner.failure = new BotExecutionException("login timed out", new RuntimeException(), new byte[]{1, 2});

        BotJobProcessor.Outcome outcome = processor.process(new JobMessage(scheduleId, "idem-fail"));

        assertThat(outcome).isEqualTo(BotJobProcessor.Outcome.FAILED);
        var execution = executions.findByIdempotencyKey("idem-fail").orElseThrow();
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.getErrorMessage()).contains("login timed out");
        assertThat(execution.getScreenshotS3Key()).isNotNull();
        assertThat(screenshotStore.stored).contains(execution.getId());
        assertThat(actions.findByExecutionIdOrderByActionTime(execution.getId())).isEmpty();
    }

    private static String uniqueSuffix() {
        return Long.toString(System.nanoTime());
    }
}
