package com.rick.supertrading.web.controller;

import com.rick.supertrading.domain.service.ExecutionQueryService;
import com.rick.supertrading.domain.service.dto.ActionView;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import com.rick.supertrading.security.CurrentUser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Historical executions and their recorded clicks, ownership-scoped. Spec §6.3. */
@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionQueryService executions;
    private final CurrentUser currentUser;

    public ExecutionController(ExecutionQueryService executions, CurrentUser currentUser) {
        this.executions = executions;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ExecutionView> list(
            @RequestParam Long scheduleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant upper = to != null ? to : Instant.now();
        Instant lower = from != null ? from : upper.minus(24, ChronoUnit.HOURS);
        return executions.listForSchedule(currentUser.require(), scheduleId, lower, upper);
    }

    @GetMapping("/{id}/actions")
    public List<ActionView> actions(@PathVariable Long id) {
        return executions.listActions(currentUser.require(), id);
    }
}
