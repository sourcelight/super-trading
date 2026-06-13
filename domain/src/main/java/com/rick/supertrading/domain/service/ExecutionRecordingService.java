package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.choice.Choice;
import com.rick.supertrading.domain.exception.ResourceNotFoundException;
import com.rick.supertrading.domain.model.Action;
import com.rick.supertrading.domain.model.Execution;
import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.model.SiteCredential;
import com.rick.supertrading.domain.repository.ActionRepository;
import com.rick.supertrading.domain.repository.ExecutionRepository;
import com.rick.supertrading.domain.repository.ScheduleRepository;
import com.rick.supertrading.domain.service.dto.BotJob;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Idempotent recording of bot runs and their clicks, used by the worker.
 *
 * <p>The {@code idempotency_key} (unique) is the hard guarantee against
 * double-recording when SQS delivers a job more than once (spec §8.2): {@link #begin}
 * returns empty when the key has already been seen, so the worker stops.
 */
@Service
public class ExecutionRecordingService {

    private final ScheduleRepository schedules;
    private final ExecutionRepository executions;
    private final ActionRepository actions;

    public ExecutionRecordingService(ScheduleRepository schedules,
                                     ExecutionRepository executions,
                                     ActionRepository actions) {
        this.schedules = schedules;
        this.executions = executions;
        this.actions = actions;
    }

    /**
     * Create the RUNNING execution and flatten the run inputs, unless this job was
     * already recorded (duplicate delivery), in which case return empty.
     */
    @Transactional
    public Optional<BotJob> begin(Long scheduleId, String idempotencyKey) {
        if (executions.existsByIdempotencyKey(idempotencyKey)) {
            return Optional.empty();
        }
        Schedule schedule = schedules.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("schedule", scheduleId));

        Execution execution;
        try {
            execution = executions.saveAndFlush(new Execution(schedule, idempotencyKey));
        } catch (DataIntegrityViolationException concurrentDuplicate) {
            // Another delivery won the race on the unique idempotency_key.
            return Optional.empty();
        }

        SiteCredential credential = schedule.getCredential();
        return Optional.of(new BotJob(
                execution.getId(),
                schedule.getId(),
                credential.getSite().getLoginUrl(),
                credential.getSite().getSelectors(),
                credential.getUsername(),
                credential.getSecretRef(),
                schedule.getActionStrategy(),
                schedule.getWaitBeforeLogoutMs()));
    }

    @Transactional
    public void recordAction(Long executionId, Choice choice, String pageUrl, int durationMs) {
        Execution execution = requireExecution(executionId);
        actions.save(new Action(execution, choice, pageUrl, durationMs));
    }

    @Transactional
    public ExecutionView completeSuccess(Long executionId) {
        Execution execution = requireExecution(executionId);
        execution.markSuccess();
        return ExecutionView.from(execution);
    }

    @Transactional
    public ExecutionView completeFailure(Long executionId, String errorMessage, String screenshotS3Key) {
        Execution execution = requireExecution(executionId);
        execution.markFailed(errorMessage, screenshotS3Key);
        return ExecutionView.from(execution);
    }

    private Execution requireExecution(Long executionId) {
        return executions.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException("execution", executionId));
    }
}
