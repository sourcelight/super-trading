package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.exception.ResourceNotFoundException;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.model.SiteCredential;
import com.rick.supertrading.domain.port.ScheduleProvisioner;
import com.rick.supertrading.domain.repository.ScheduleRepository;
import com.rick.supertrading.domain.repository.SiteCredentialRepository;
import com.rick.supertrading.domain.service.dto.CreateScheduleCommand;
import com.rick.supertrading.domain.service.dto.ScheduleView;
import com.rick.supertrading.domain.service.dto.UpdateScheduleCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages schedules and keeps the external (EventBridge) timer in sync via the
 * {@link ScheduleProvisioner} port. All operations are ownership-scoped through
 * the schedule's credential; ADMINs bypass the filter.
 */
@Service
public class ScheduleService {

    private final ScheduleRepository schedules;
    private final SiteCredentialRepository credentials;
    private final ScheduleProvisioner provisioner;
    private final AuditService audit;

    public ScheduleService(ScheduleRepository schedules,
                           SiteCredentialRepository credentials,
                           ScheduleProvisioner provisioner,
                           AuditService audit) {
        this.schedules = schedules;
        this.credentials = credentials;
        this.provisioner = provisioner;
        this.audit = audit;
    }

    @Transactional
    public ScheduleView create(AppUser owner, CreateScheduleCommand cmd) {
        SiteCredential credential = loadOwnedCredential(owner, cmd.credentialId());

        Schedule schedule = schedules.save(new Schedule(
                credential, cmd.intervalSeconds(), cmd.actionStrategy(), cmd.waitBeforeLogoutMs()));

        String arn = provisioner.create(schedule);
        if (arn != null) {
            schedule.setEventbridgeScheduleArn(arn);
        }

        audit.record(owner.getId(), "schedule", schedule.getId(), "CREATE",
                Map.of("credentialId", credential.getId(), "intervalSeconds", cmd.intervalSeconds()));
        return ScheduleView.from(schedule);
    }

    @Transactional
    public ScheduleView update(AppUser owner, Long id, UpdateScheduleCommand cmd) {
        Schedule schedule = loadOwned(owner, id);
        Map<String, Object> changes = new HashMap<>();

        if (cmd.intervalSeconds() != null) {
            schedule.setIntervalSeconds(cmd.intervalSeconds());
            changes.put("intervalSeconds", cmd.intervalSeconds());
        }
        if (cmd.actionStrategy() != null) {
            schedule.setActionStrategy(cmd.actionStrategy());
            changes.put("actionStrategy", cmd.actionStrategy());
        }
        if (cmd.waitBeforeLogoutMs() != null) {
            schedule.setWaitBeforeLogoutMs(cmd.waitBeforeLogoutMs());
            changes.put("waitBeforeLogoutMs", cmd.waitBeforeLogoutMs());
        }
        boolean enabledToggled = cmd.enabled() != null && cmd.enabled() != schedule.isEnabled();
        if (cmd.enabled() != null) {
            schedule.setEnabled(cmd.enabled());
            changes.put("enabled", cmd.enabled());
        }

        provisioner.update(schedule);

        String action = enabledToggled ? (schedule.isEnabled() ? "ENABLE" : "DISABLE") : "UPDATE";
        audit.record(owner.getId(), "schedule", schedule.getId(), action, changes);
        return ScheduleView.from(schedule);
    }

    @Transactional
    public void delete(AppUser owner, Long id) {
        Schedule schedule = loadOwned(owner, id);
        provisioner.delete(schedule.getEventbridgeScheduleArn());
        schedules.delete(schedule);
        audit.record(owner.getId(), "schedule", id, "DELETE", null);
    }

    @Transactional(readOnly = true)
    public List<ScheduleView> list(AppUser user) {
        List<Schedule> result = user.isAdmin()
                ? schedules.findAll()
                : schedules.findByCredentialOwnerId(user.getId());
        return result.stream().map(ScheduleView::from).toList();
    }

    private SiteCredential loadOwnedCredential(AppUser user, Long credentialId) {
        return (user.isAdmin()
                ? credentials.findById(credentialId)
                : credentials.findByIdAndOwnerId(credentialId, user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("credential", credentialId));
    }

    private Schedule loadOwned(AppUser user, Long id) {
        return (user.isAdmin()
                ? schedules.findById(id)
                : schedules.findByIdAndCredentialOwnerId(id, user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("schedule", id));
    }
}
