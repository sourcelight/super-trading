package com.rick.supertrading.domain;

import com.rick.supertrading.domain.port.MonitoringPublisher;
import com.rick.supertrading.domain.port.ScreenshotStore;
import com.rick.supertrading.domain.port.SecretReader;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import com.rick.supertrading.worker.BotJobProcessor;
import com.rick.supertrading.worker.bot.BotExecutionException;
import com.rick.supertrading.worker.bot.BotResult;
import com.rick.supertrading.worker.bot.BotRunner;
import com.rick.supertrading.worker.bot.BotSessionRequest;
import com.rick.supertrading.worker.config.WorkerProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bootstrap for the worker data-path e2e. Placed at {@code com.rick.supertrading.domain}
 * so default scanning picks up the domain services/repositories/strategy <em>but not</em>
 * the worker's AWS/SQS infrastructure. {@link BotJobProcessor} is imported explicitly and
 * wired to in-memory fakes for the AWS-backed ports and the browser, so the test exercises
 * the real DB, real recording/idempotency logic, and real strategy resolution.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(BotJobProcessor.class)
public class WorkerE2ETestConfig {

    @Bean
    public WorkerProperties workerProperties() {
        WorkerProperties properties = new WorkerProperties();
        properties.setNavigateMillis(0);
        return properties;
    }

    @Bean
    public SecretReader secretReader() {
        return secretRef -> "test-password";
    }

    @Bean
    public FakeBotRunner botRunner() {
        return new FakeBotRunner();
    }

    @Bean
    public FakeScreenshotStore screenshotStore() {
        return new FakeScreenshotStore();
    }

    @Bean
    public RecordingMonitoringPublisher monitoringPublisher() {
        return new RecordingMonitoringPublisher();
    }

    /** Bot stand-in: returns {@link #result} unless {@link #failure} is set. */
    public static class FakeBotRunner implements BotRunner {
        public volatile BotResult result = new BotResult("https://mock.local/page", 42);
        public volatile BotExecutionException failure;

        @Override
        public BotResult runSession(BotSessionRequest request) throws BotExecutionException {
            if (failure != null) {
                throw failure;
            }
            return result;
        }
    }

    public static class FakeScreenshotStore implements ScreenshotStore {
        public final List<Long> stored = new CopyOnWriteArrayList<>();

        @Override
        public String store(Long executionId, byte[] pngBytes) {
            stored.add(executionId);
            return "screenshots/" + executionId + ".png";
        }
    }

    public static class RecordingMonitoringPublisher implements MonitoringPublisher {
        public final List<ExecutionView> published = new CopyOnWriteArrayList<>();

        @Override
        public void publish(ExecutionView execution) {
            published.add(execution);
        }
    }
}
