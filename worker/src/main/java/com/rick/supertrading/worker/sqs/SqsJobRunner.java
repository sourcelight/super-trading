package com.rick.supertrading.worker.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rick.supertrading.worker.BotJobProcessor;
import com.rick.supertrading.worker.JobMessage;
import com.rick.supertrading.worker.config.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

/**
 * Drains the SQS queue once and exits — the Fargate-task model (spec §4: "runs
 * then exits"). A message is deleted only after its job reaches a recorded outcome
 * (success, recorded failure, or duplicate); if processing throws (infrastructure
 * error), the message is left for SQS redelivery / DLQ with backoff.
 *
 * <p>Active in every profile except {@code local}; locally a long-running
 * {@code LocalSqsPoller} consumes continuously so a short-interval demo keeps flowing.
 */
@Component
@Profile("!local")
public class SqsJobRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SqsJobRunner.class);

    private final SqsClient sqs;
    private final BotJobProcessor processor;
    private final ObjectMapper objectMapper;
    private final WorkerProperties properties;

    public SqsJobRunner(SqsClient sqs, BotJobProcessor processor,
                        ObjectMapper objectMapper, WorkerProperties properties) {
        this.sqs = sqs;
        this.processor = processor;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String queueUrl = properties.getQueueUrl();
        log.info("Worker draining queue {}", queueUrl);
        int processed = 0;
        while (true) {
            List<Message> messages = sqs.receiveMessage(b -> b
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(properties.getMaxMessages())
                    .waitTimeSeconds(properties.getWaitTimeSeconds())).messages();
            if (messages.isEmpty()) {
                break;
            }
            for (Message message : messages) {
                if (handle(queueUrl, message)) {
                    processed++;
                }
            }
        }
        log.info("Worker finished; {} job(s) processed", processed);
    }

    /** @return true if the message was processed and deleted */
    private boolean handle(String queueUrl, Message message) {
        try {
            JobMessage job = objectMapper.readValue(message.body(), JobMessage.class);
            BotJobProcessor.Outcome outcome = processor.process(job);
            sqs.deleteMessage(b -> b.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
            log.debug("Message {} -> {} (deleted)", message.messageId(), outcome);
            return true;
        } catch (Exception ex) {
            // Leave the message for redelivery; SQS visibility timeout + DLQ handle retries.
            log.error("Message {} failed to process; leaving for redelivery: {}",
                    message.messageId(), ex.getMessage(), ex);
            return false;
        }
    }
}
