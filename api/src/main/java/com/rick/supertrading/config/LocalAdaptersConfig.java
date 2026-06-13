package com.rick.supertrading.config;

import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.port.ScheduleProvisioner;
import com.rick.supertrading.domain.port.SecretStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

/**
 * Non-local stand-ins for the AWS-backed ports so the API runs without a cloud account
 * (e.g. the default profile in dev/tests). Gated {@code @Profile("!local")}: the local
 * profile supplies real LocalStack/in-JVM adapters ({@code LocalSecretsConfig},
 * {@code LocalScheduleTrigger}) instead. {@code @ConditionalOnMissingBean} still lets a real
 * cloud adapter replace these when one is added.
 */
@Configuration
@Profile("!local")
public class LocalAdaptersConfig {

    private static final Logger log = LoggerFactory.getLogger(LocalAdaptersConfig.class);

    /** Stores nothing; returns a synthetic reference. The password is never logged. */
    @Bean
    @ConditionalOnMissingBean(SecretStore.class)
    public SecretStore localSecretStore() {
        return (name, secretValue) -> {
            String ref = "local-secret://" + name + "#" + UUID.randomUUID();
            log.info("Stored secret reference for '{}' (value withheld): {}", name, ref);
            return ref;
        };
    }

    /**
     * No external timer; just stamps a synthetic reference so linkage is visible.
     * Disabled (in favour of the EventBridge adapter) when
     * {@code supertrading.scheduling.enabled=true}.
     */
    @Bean
    @ConditionalOnProperty(name = "supertrading.scheduling.enabled", havingValue = "false", matchIfMissing = true)
    public ScheduleProvisioner localScheduleProvisioner() {
        return new ScheduleProvisioner() {
            @Override
            public String create(Schedule schedule) {
                String ref = "local-schedule://" + schedule.getId();
                log.info("Provisioned local schedule {} (interval={}s)",
                        ref, schedule.getIntervalSeconds());
                return ref;
            }

            @Override
            public void update(Schedule schedule) {
                log.info("Updated local schedule for id={} (enabled={})",
                        schedule.getId(), schedule.isEnabled());
            }

            @Override
            public void delete(String scheduleArn) {
                if (scheduleArn != null) {
                    log.info("Deleted local schedule {}", scheduleArn);
                }
            }
        };
    }
}
