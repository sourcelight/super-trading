package com.rick.supertrading.worker.config;

import com.rick.supertrading.domain.model.Schedule;
import com.rick.supertrading.domain.port.ScheduleProvisioner;
import com.rick.supertrading.domain.port.SecretStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerSchedulingConfig {

    @Bean
    @ConditionalOnMissingBean
    public ScheduleProvisioner scheduleProvisioner() {
        return new ScheduleProvisioner() {
            @Override
            public String create(Schedule schedule) {
                return null;
            }

            @Override
            public void update(Schedule schedule) {
            }

            @Override
            public void delete(String scheduleArn) {
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public SecretStore secretStore() {
        return new SecretStore() {
            @Override
            public String store(String name, String secretValue) {
                return null;
            }
        };
    }
}
