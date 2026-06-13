package com.rick.supertrading.scheduling;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the EventBridge Scheduler adapter, bound from
 * {@code supertrading.scheduling.*}. When {@code enabled} is false (the default),
 * the API falls back to its local no-op provisioner.
 */
@ConfigurationProperties(prefix = "supertrading.scheduling")
public class SchedulingProperties {

    /** Master switch; when false the real EventBridge adapter is not wired. */
    private boolean enabled = false;

    /** AWS region for the Scheduler client (e.g. eu-west-1). */
    private String region;

    /** Logical group all schedules are created in. */
    private String groupName = "default";

    /** ARN of the SQS queue that each schedule targets on fire. */
    private String queueArn;

    /** ARN of the IAM role EventBridge assumes to deliver to the queue. */
    private String schedulerRoleArn;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getQueueArn() {
        return queueArn;
    }

    public void setQueueArn(String queueArn) {
        this.queueArn = queueArn;
    }

    public String getSchedulerRoleArn() {
        return schedulerRoleArn;
    }

    public void setSchedulerRoleArn(String schedulerRoleArn) {
        this.schedulerRoleArn = schedulerRoleArn;
    }
}
