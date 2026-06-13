package com.rick.supertrading.security;

import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.service.AppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Local-profile {@link CurrentUser}: resolves a fixed demo ADMIN user (no Cognito). The
 * user is just-in-time provisioned on first use, matching the subject seeded by the local
 * seed runner so the demo data is owned by this same user.
 */
@Component
@Profile("local")
public class LocalCurrentUserService implements CurrentUser {

    /** Stable subject for the local demo user; mirrored by the local seed. */
    public static final String DEMO_SUB = "local-demo-sub";
    public static final String DEMO_EMAIL = "demo@local.test";

    private final AppUserService appUserService;

    public LocalCurrentUserService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public AppUser require() {
        return appUserService.getOrProvision(DEMO_SUB, DEMO_EMAIL, "Demo User", true);
    }
}
