package com.rick.supertrading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;

/**
 * Console authentication via Amazon Cognito (OIDC): the API is an OAuth2 resource
 * server validating JWTs. Cognito group membership ({@code cognito:groups}) is
 * mapped to Spring authorities, so a user in the {@code ADMIN} group gets
 * {@code ROLE_ADMIN}. Coarse URL rules live here; fine-grained ownership checks
 * live in the services, with {@code @PreAuthorize} available via method security.
 *
 * <p>Active in every profile except {@code local}; the local profile uses
 * {@code LocalSecurityConfig} (dev no-auth) instead.
 */
@Configuration
@EnableWebSecurity
@Profile("!local")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Stateless JWT API: CSRF protection is not applicable.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Handshake is currently open; the STOMP layer carries no mutations.
                        // TODO(step-3-followup): authenticate the WS handshake via a token.
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // ADMIN-only console area (spec §10 screen 7).
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /** Maps {@code cognito:groups} entries to {@code ROLE_<group>} authorities. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::extractAuthorities);
        return converter;
    }

    private static Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object groups = jwt.getClaim("cognito:groups");
        if (groups instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .map(g -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + g))
                    .toList();
        }
        return List.of();
    }
}
