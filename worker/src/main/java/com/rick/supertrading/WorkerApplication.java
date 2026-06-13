package com.rick.supertrading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bot worker entry point (spec §4: Fargate task). Boots at the
 * {@code com.rick.supertrading} root so component, entity and repository scanning
 * pick up the shared {@code domain} module; then {@code SqsJobRunner} drains the
 * queue and the JVM exits.
 */
@SpringBootApplication
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
