package com.rick.supertrading.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rick.supertrading.domain.repository.ScheduleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

/**
 * Wires the in-JVM {@link LocalScheduleTrigger} under the {@code local} profile. The
 * EventBridge {@code SchedulingConfig} stays off locally (its
 * {@code supertrading.scheduling.enabled} property is false), so this trigger provides the
 * {@link com.rick.supertrading.domain.port.ScheduleProvisioner} instead.
 */
@Configuration
@Profile("local")
@EnableConfigurationProperties(LocalSchedulingProperties.class)
public class LocalSchedulingConfig {

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler localTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("local-sched-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean(name = "localSchedulerSqsClient")
    public SqsClient localSchedulerSqsClient(LocalSchedulingProperties properties) {
        SqsClientBuilder builder = SqsClient.builder().region(Region.of(properties.getRegion()));
        if (properties.getSqsEndpoint() != null) {
            builder.endpointOverride(URI.create(properties.getSqsEndpoint()));
        }
        builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        return builder.build();
    }

    @Bean
    public LocalScheduleTrigger localScheduleTrigger(TaskScheduler localTaskScheduler,
                                                     SqsClient localSchedulerSqsClient,
                                                     ObjectMapper objectMapper,
                                                     ScheduleRepository scheduleRepository,
                                                     LocalSchedulingProperties properties) {
        return new LocalScheduleTrigger(
                localTaskScheduler, localSchedulerSqsClient, objectMapper, scheduleRepository, properties);
    }
}
