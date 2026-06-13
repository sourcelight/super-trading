package com.rick.supertrading;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Supplies a stub {@link JwtDecoder} so the OAuth2 resource-server autoconfiguration
 * backs off and the context boots without contacting a real Cognito issuer. The
 * decoder is never exercised by {@code contextLoads} (no request is authenticated).
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            throw new UnsupportedOperationException("stub JwtDecoder is not meant to decode tokens");
        };
    }
}
