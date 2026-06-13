package com.rick.supertrading.worker;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.domain.choice.ChoiceContext;
import com.rick.supertrading.domain.choice.ChoiceStrategyResolver;
import com.rick.supertrading.domain.port.MonitoringPublisher;
import com.rick.supertrading.domain.port.ScreenshotStore;
import com.rick.supertrading.domain.port.SecretReader;
import com.rick.supertrading.domain.service.ExecutionRecordingService;
import com.rick.supertrading.domain.service.dto.BotJob;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import com.rick.supertrading.worker.bot.BotExecutionException;
import com.rick.supertrading.worker.bot.BotResult;
import com.rick.supertrading.worker.bot.BotRunner;
import com.rick.supertrading.worker.bot.BotSessionRequest;
import com.rick.supertrading.worker.config.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Orchestrates one job end to end (spec §8.1): idempotency guard → create
 * execution → read secret → resolve strategy → run the browser session → record
 * the click and outcome → push live status. A bot failure is recorded (with a
 * screenshot) and reported as {@link Outcome#FAILED}; both FAILED and SUCCESS are
 * terminal for the job's idempotency key. Infrastructure errors are thrown so the
 * caller can leave the message for SQS redelivery.
 */
@Component
public class BotJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(BotJobProcessor.class);

    public enum Outcome {DUPLICATE, SUCCESS, FAILED}

    private final ExecutionRecordingService recording;
    private final ChoiceStrategyResolver strategyResolver;
    private final SecretReader secretReader;
    private final ScreenshotStore screenshotStore;
    private final MonitoringPublisher monitoring;
    private final BotRunner botRunner;
    private final WorkerProperties properties;

    public BotJobProcessor(ExecutionRecordingService recording,
                           ChoiceStrategyResolver strategyResolver,
                           SecretReader secretReader,
                           ScreenshotStore screenshotStore,
                           MonitoringPublisher monitoring,
                           BotRunner botRunner,
                           WorkerProperties properties) {
        this.recording = recording;
        this.strategyResolver = strategyResolver;
        this.secretReader = secretReader;
        this.screenshotStore = screenshotStore;
        this.monitoring = monitoring;
        this.botRunner = botRunner;
        this.properties = properties;
    }

    public Outcome process(JobMessage message) {
        Optional<BotJob> jobOpt = recording.begin(message.scheduleId(), message.idempotencyKey());
        if (jobOpt.isEmpty()) {
            log.info("Skipping duplicate delivery for idempotencyKey={}", message.idempotencyKey());
            return Outcome.DUPLICATE;
        }
        BotJob job = jobOpt.get();

        try {
            String password = secretReader.read(job.secretRef());
            Choice choice = strategyResolver.resolve(job.actionStrategy())
                    .decide(ChoiceContext.forSchedule(job.scheduleId()));

            BotResult result = botRunner.runSession(new BotSessionRequest(
                    job.loginUrl(), job.selectors(), job.username(), password, choice,
                    properties.getNavigateMillis(), job.waitBeforeLogoutMs()));

            recording.recordAction(job.executionId(), choice, result.pageUrl(), result.clickDurationMs());
            ExecutionView view = recording.completeSuccess(job.executionId());
            monitoring.publish(view);
            return Outcome.SUCCESS;
        } catch (BotExecutionException failure) {
            String screenshotKey = failure.getScreenshot() != null
                    ? screenshotStore.store(job.executionId(), failure.getScreenshot())
                    : null;
            ExecutionView view = recording.completeFailure(job.executionId(), failure.getMessage(), screenshotKey);
            monitoring.publish(view);
            log.warn("Execution {} failed: {}", job.executionId(), failure.getMessage());
            return Outcome.FAILED;
        }
    }
}
