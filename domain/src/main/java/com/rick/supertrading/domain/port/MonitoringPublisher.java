package com.rick.supertrading.domain.port;

import com.rick.supertrading.domain.service.dto.ExecutionView;

/**
 * Outbound port for pushing live run status to the admin console. The API binds
 * this to a STOMP/WebSocket adapter; other runtimes (e.g. the worker) may bind a
 * no-op.
 */
public interface MonitoringPublisher {

    /** Push the current state of an execution to subscribers. */
    void publish(ExecutionView execution);
}
