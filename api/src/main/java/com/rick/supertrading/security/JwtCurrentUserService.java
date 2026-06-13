package com.rick.supertrading.security;

import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.service.AppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Cloud-profile {@link CurrentUser}: resolves the user from the Cognito JWT in the security
 * context, just-in-time provisioning the row on first sight. Active in every profile except
 * {@code local} (where a fixed demo user is used instead).
 */
@Component
@Profile("!local")
public class JwtCurrentUserService implements CurrentUser {

    private final AppUserService appUserService;

    public JwtCurrentUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public AppUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No authenticated JWT in the security context");
        }
        boolean admin = groups(jwt).contains("ADMIN");
        return appUserService.getOrProvision(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("name"),
                admin);
    }

    private List<String> groups(Jwt jwt) {
        Object groups = jwt.getClaim("cognito:groups");
        if (groups instanceof Collection<?> collection) {
            return collection.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}
