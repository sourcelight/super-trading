package com.rick.supertrading.ws;

import com.rick.supertrading.domain.port.MonitoringPublisher;
import com.rick.supertrading.domain.service.dto.ExecutionView;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Binds the domain {@link MonitoringPublisher} port to STOMP: pushes execution
 * status updates to subscribers of {@code /topic/monitoring}. Used by the worker
 * flow (Step 5) to surface live progress to the console.
 */
@Component
public class StompMonitoringPublisher implements MonitoringPublisher {

    static final String DESTINATION = "/topic/monitoring";

    private final SimpMessagingTemplate messagingTemplate;

    public StompMonitoringPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(ExecutionView execution) {
        messagingTemplate.convertAndSend(DESTINATION, execution);
    }
}
