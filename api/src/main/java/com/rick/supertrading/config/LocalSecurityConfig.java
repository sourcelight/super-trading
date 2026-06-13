package com.rick.supertrading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Local-profile dev security: permits all requests (no Cognito, no JWT) and grants the
 * anonymous principal {@code ROLE_ADMIN} so {@code @PreAuthorize("hasRole('ADMIN')")} on the
 * admin endpoints passes for the demo user. The {@code local} profile thus boots without any
 * {@code issuer-uri}. Replaced by {@code SecurityConfig} in all other profiles.
 */
@Configuration
@EnableWebSecurity
@Profile("local")
public class LocalSecurityConfig {

    @Bean
    public SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .anonymous(anon -> anon.principal("local-demo").authorities("ROLE_ADMIN"));
        return http.build();
    }
}
