package com.rick.supertrading.domain.port;

import com.rick.supertrading.domain.model.Schedule;

/**
 * Outbound port for provisioning the durable, external timer behind a
 * {@link Schedule}. Production binds this to AWS EventBridge Scheduler (Step 4);
 * a no-op adapter is used until then.
 */
public interface ScheduleProvisioner {

    /**
     * Provision the external schedule for a freshly persisted {@link Schedule}.
     *
     * @return the external schedule reference (e.g. EventBridge ARN), or null if none
     */
    String create(Schedule schedule);

    /** Sync the external schedule after interval/enabled/strategy changes. */
    void update(Schedule schedule);

    /** Remove the external schedule. No-op if {@code scheduleArn} is null. */
    void delete(String scheduleArn);
}
