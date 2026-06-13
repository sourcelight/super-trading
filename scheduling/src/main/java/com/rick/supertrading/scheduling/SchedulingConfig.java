package com.rick.supertrading.scheduling;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;
import software.amazon.awssdk.services.scheduler.SchedulerClientBuilder;

/**
 * Wires the real EventBridge {@link EventBridgeScheduleManager} when
 * {@code supertrading.scheduling.enabled=true}. Otherwise nothing here activates
 * and the API's local no-op provisioner remains in effect.
 */
@Configuration
@EnableConfigurationProperties(SchedulingProperties.class)
@ConditionalOnProperty(name = "supertrading.scheduling.enabled", havingValue = "true")
public class SchedulingConfig {

    @Bean
    @ConditionalOnMissingBean
    public SchedulerClient schedulerClient(SchedulingProperties properties) {
        SchedulerClientBuilder builder = SchedulerClient.builder();
        if (properties.getRegion() != null) {
            builder.region(Region.of(properties.getRegion()));
        }
        return builder.build();
    }

    @Bean
    public EventBridgeScheduleManager eventBridgeScheduleManager(SchedulerClient client,
                                                                 SchedulingProperties properties) {
        return new EventBridgeScheduleManager(client, properties);
    }
}
