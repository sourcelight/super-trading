package com.rick.supertrading.domain;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Minimal Spring Boot bootstrap for testing the shared domain library in
 * isolation (it has no application module of its own). Placed at the
 * {@code com.rick.supertrading.domain} root so default entity and repository
 * scanning picks up the {@code .model} and {@code .repository} packages.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class DomainTestApp {
}
