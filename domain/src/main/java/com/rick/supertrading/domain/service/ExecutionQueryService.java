package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.exception.ResourceNotFoundException;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Execution;
import com.rick.supertrading.domain.repository.ActionRepository;
import com.rick.supertrading.domain.repository.ExecutionRepository;
import com.rick.supertrading.domain.repository.ScheduleRepository;
import com.rick.supertrading.domain.service.dto.ActionView;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Read-only historical queries over executions and their actions, ownership-scoped
 * through the schedule → credential → owner chain (ADMIN bypasses).
 */
@Service
public class ExecutionQueryService {

    private final ScheduleRepository schedules;
    private final ExecutionRepository executions;
    private final ActionRepository actions;

    public ExecutionQueryService(ScheduleRepository schedules,
                                 ExecutionRepository executions,
                                 ActionRepository actions) {
        this.schedules = schedules;
        this.executions = executions;
        this.actions = actions;
    }

    @Transactional(readOnly = true)
    public List<ExecutionView> listForSchedule(AppUser user, Long scheduleId, Instant from, Instant to) {
        boolean visible = user.isAdmin()
                || schedules.findByIdAndCredentialOwnerId(scheduleId, user.getId()).isPresent();
        if (!visible) {
            throw new ResourceNotFoundException("schedule", scheduleId);
        }
        return executions
                .findByScheduleIdAndStartedAtBetweenOrderByStartedAtDesc(scheduleId, from, to)
                .stream().map(ExecutionView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ActionView> listActions(AppUser user, Long executionId) {
        Execution execution = (user.isAdmin()
                ? executions.findById(executionId)
                : executions.findByIdAndScheduleCredentialOwnerId(executionId, user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("execution", executionId));
        return actions.findByExecutionIdOrderByActionTime(execution.getId())
                .stream().map(ActionView::from).toList();
    }
}
