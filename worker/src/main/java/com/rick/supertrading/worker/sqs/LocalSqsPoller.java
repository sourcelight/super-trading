package com.rick.supertrading.worker.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rick.supertrading.worker.BotJobProcessor;
import com.rick.supertrading.worker.JobMessage;
import com.rick.supertrading.worker.config.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

/**
 * Local-profile continuous consumer: long-polls the queue on a background thread and keeps
 * the worker JVM alive, so a short-interval demo flows without re-running the process. Uses
 * the same parse → {@link BotJobProcessor#process} → delete-on-recorded-outcome logic as the
 * production {@link SqsJobRunner} (which is drain-and-exit for the Fargate-task model).
 */
@Component
@Profile("local")
public class LocalSqsPoller implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(LocalSqsPoller.class);

    private final SqsClient sqs;
    private final BotJobProcessor processor;
    private final ObjectMapper objectMapper;
    private final WorkerProperties properties;

    private volatile boolean running = false;
    private Thread thread;

    public LocalSqsPoller(SqsClient sqs, BotJobProcessor processor,
                          ObjectMapper objectMapper, WorkerProperties properties) {
        this.sqs = sqs;
        this.processor = processor;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void start() {
        running = true;
        thread = new Thread(this::loop, "local-sqs-poller");
        thread.setDaemon(false); // keep the JVM alive while consuming
        thread.start();
        log.info("LocalSqsPoller consuming {}", properties.getQueueUrl());
    }

    private void loop() {
        String queueUrl = properties.getQueueUrl();
        while (running) {
            try {
                List<Message> messages = sqs.receiveMessage(b -> b
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(properties.getMaxMessages())
                        .waitTimeSeconds(properties.getWaitTimeSeconds())).messages();
                for (Message message : messages) {
                    handle(queueUrl, message);
                }
            } catch (Exception ex) {
                if (running) {
                    log.error("Poll loop error: {}", ex.getMessage(), ex);
                }
            }
        }
    }

    private void handle(String queueUrl, Message message) {
        try {
            JobMessage job = objectMapper.readValue(message.body(), JobMessage.class);
            BotJobProcessor.Outcome outcome = processor.process(job);
            sqs.deleteMessage(b -> b.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
            log.debug("Message {} -> {} (deleted)", message.messageId(), outcome);
        } catch (Exception ex) {
            log.error("Message {} failed; leaving for redelivery: {}", message.messageId(), ex.getMessage(), ex);
        }
    }

    @Override
    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
