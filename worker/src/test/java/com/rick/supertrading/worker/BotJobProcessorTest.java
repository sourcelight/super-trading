package com.rick.supertrading.worker;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.domain.choice.ChoiceStrategy;
import com.rick.supertrading.domain.choice.ChoiceStrategyResolver;
import com.rick.supertrading.domain.model.ExecutionStatus;
import com.rick.supertrading.domain.port.MonitoringPublisher;
import com.rick.supertrading.domain.port.ScreenshotStore;
import com.rick.supertrading.domain.port.SecretReader;
import com.rick.supertrading.domain.service.ExecutionRecordingService;
import com.rick.supertrading.domain.service.dto.BotJob;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import com.rick.supertrading.worker.bot.BotExecutionException;
import com.rick.supertrading.worker.bot.BotResult;
import com.rick.supertrading.worker.bot.BotRunner;
import com.rick.supertrading.worker.config.WorkerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BotJobProcessorTest {

    private final ExecutionRecordingService recording = mock(ExecutionRecordingService.class);
    private final ChoiceStrategyResolver resolver = mock(ChoiceStrategyResolver.class);
    private final SecretReader secretReader = mock(SecretReader.class);
    private final ScreenshotStore screenshotStore = mock(ScreenshotStore.class);
    private final MonitoringPublisher monitoring = mock(MonitoringPublisher.class);
    private final BotRunner botRunner = mock(BotRunner.class);
    private final WorkerProperties properties = new WorkerProperties();

    private BotJobProcessor processor;

    private static final JobMessage MESSAGE = new JobMessage(7L, "idem-1");

    @BeforeEach
    void setUp() {
        processor = new BotJobProcessor(recording, resolver, secretReader, screenshotStore,
                monitoring, botRunner, properties);
    }

    @Test
    void skipsDuplicateDeliveryWithoutRunningTheBot() throws Exception {
        when(recording.begin(7L, "idem-1")).thenReturn(Optional.empty());

        assertThat(processor.process(MESSAGE)).isEqualTo(BotJobProcessor.Outcome.DUPLICATE);
        verify(botRunner, never()).runSession(any());
    }

    @Test
    void recordsActionAndSuccessOnHappyPath() throws Exception {
        givenJob();
        when(secretReader.read("arn:secret")).thenReturn("hunter2");
        ChoiceStrategy green = ctx -> Choice.GREEN;
        when(resolver.resolve("random")).thenReturn(green);
        when(botRunner.runSession(any())).thenReturn(new BotResult("https://site/page", 120));
        when(recording.completeSuccess(99L)).thenReturn(view(ExecutionStatus.SUCCESS));

        assertThat(processor.process(MESSAGE)).isEqualTo(BotJobProcessor.Outcome.SUCCESS);
        verify(recording).recordAction(99L, Choice.GREEN, "https://site/page", 120);
        verify(recording).completeSuccess(99L);
        verify(monitoring).publish(any());
    }

    @Test
    void recordsFailureAndUploadsScreenshotOnBotError() throws Exception {
        givenJob();
        when(secretReader.read("arn:secret")).thenReturn("hunter2");
        when(resolver.resolve("random")).thenReturn((ChoiceStrategy) ctx -> Choice.RED);
        byte[] shot = {1, 2, 3};
        when(botRunner.runSession(any()))
                .thenThrow(new BotExecutionException("boom", new RuntimeException(), shot));
        when(screenshotStore.store(99L, shot)).thenReturn("screenshots/99/x.png");
        when(recording.completeFailure(eq(99L), any(), eq("screenshots/99/x.png")))
                .thenReturn(view(ExecutionStatus.FAILED));

        assertThat(processor.process(MESSAGE)).isEqualTo(BotJobProcessor.Outcome.FAILED);
        verify(screenshotStore).store(99L, shot);
        verify(recording).completeFailure(99L, "boom", "screenshots/99/x.png");
        verify(recording, never()).completeSuccess(any());
    }

    private void givenJob() {
        BotJob job = new BotJob(99L, 7L, "https://site/login",
                Map.of("username", "#u", "password", "#p", "green", ".g", "red", ".r", "logout", "#o"),
                "bot-user", "arn:secret", "random", 3000);
        when(recording.begin(7L, "idem-1")).thenReturn(Optional.of(job));
    }

    private static ExecutionView view(ExecutionStatus status) {
        return new ExecutionView(99L, 7L, status, Instant.now(), Instant.now(), null, null);
    }
}
