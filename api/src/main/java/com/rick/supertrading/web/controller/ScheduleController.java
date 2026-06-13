package com.rick.supertrading.web.controller;

import com.rick.supertrading.domain.service.ScheduleService;
import com.rick.supertrading.domain.service.dto.ScheduleView;
import com.rick.supertrading.security.CurrentUser;
import com.rick.supertrading.web.dto.CreateScheduleRequest;
import com.rick.supertrading.web.dto.UpdateScheduleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Schedules against owned credentials; create/update/delete also sync the external
 * (EventBridge) timer through the provisioner port. Spec §6.3.
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final CurrentUser currentUser;

    public ScheduleController(ScheduleService scheduleService, CurrentUser currentUser) {
        this.scheduleService = scheduleService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<ScheduleView> create(@Valid @RequestBody CreateScheduleRequest request) {
        ScheduleView created = scheduleService.create(currentUser.require(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<ScheduleView> list() {
        return scheduleService.list(currentUser.require());
    }

    @PutMapping("/{id}")
    public ScheduleView update(@PathVariable Long id, @Valid @RequestBody UpdateScheduleRequest request) {
        return scheduleService.update(currentUser.require(), id, request.toCommand());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(currentUser.require(), id);
        return ResponseEntity.noContent().build();
    }
}
