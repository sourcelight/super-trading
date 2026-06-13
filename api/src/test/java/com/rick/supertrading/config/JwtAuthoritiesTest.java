package com.rick.supertrading.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the custom Cognito group → Spring authority mapping, exercised
 * through the real {@link JwtAuthenticationConverter} bean (no Spring context).
 */
class JwtAuthoritiesTest {

    private final JwtAuthenticationConverter converter = new SecurityConfig().jwtAuthenticationConverter();

    @Test
    void mapsCognitoGroupsToRoleAuthorities() {
        Authentication auth = converter.convert(jwtWithGroups(List.of("ADMIN", "ops")));

        assertThat(auth.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_ops");
    }

    @Test
    void yieldsNoRolesWhenGroupsClaimAbsent() {
        Authentication auth = converter.convert(jwtWithGroups(null));

        assertThat(auth.getAuthorities())
                .noneMatch(a -> a.getAuthority().startsWith("ROLE_"));
    }

    private static Jwt jwtWithGroups(List<String> groups) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-sub")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        if (groups != null) {
            builder.claim("cognito:groups", groups);
        }
        return builder.build();
    }
}
