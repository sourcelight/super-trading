package com.rick.supertrading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables {@code @PreAuthorize} method security in all profiles, so admin checks are
 * enforced consistently whether the active security chain is the Cognito resource server
 * ({@code !local}) or the local dev chain.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
