package com.rick.supertrading.worker.monitor;

import com.rick.supertrading.domain.port.MonitoringPublisher;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Worker-side {@link MonitoringPublisher}: logs run-status transitions.
 *
 * <p>The live console push happens in the API process (its STOMP adapter). Bridging
 * the worker's status to that WebSocket across processes (e.g. via SNS or a status
 * queue the API relays) is a deliberate follow-up; for now the worker emits
 * structured logs that also drive observability.
 */
@Component
public class LoggingMonitoringPublisher implements MonitoringPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingMonitoringPublisher.class);

    @Override
    public void publish(ExecutionView execution) {
        log.info("execution {} schedule {} -> {}",
                execution.id(), execution.scheduleId(), execution.status());
    }
}
