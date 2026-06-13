package com.rick.supertrading.local;

import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Site;
import com.rick.supertrading.domain.model.SiteCredential;
import com.rick.supertrading.domain.port.SecretStore;
import com.rick.supertrading.domain.repository.SiteCredentialRepository;
import com.rick.supertrading.domain.repository.SiteRepository;
import com.rick.supertrading.domain.service.AppUserService;
import com.rick.supertrading.security.LocalCurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Seeds demo data for the {@code local} profile so the simulation has something to run
 * immediately: the demo ADMIN user (matching {@link LocalCurrentUserService}), the mock
 * target site whose selectors match {@code mock-target-site/*.html}, and one credential whose
 * secret is written to the local secret store (LocalStack) so the worker can read it.
 *
 * <p>Idempotent — only inserts what's missing. Does NOT create a schedule; the demo creates a
 * short-interval schedule via the console to exercise {@code LocalScheduleTrigger}.
 */
@Component
@Profile("local")
public class LocalSeedRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalSeedRunner.class);

    // Must match mock-target-site/login.html + dashboard.html element ids.
    private static final String BASE_URL = "http://localhost:8088";
    private static final String LOGIN_URL = "http://localhost:8088/login.html";
    private static final Map<String, String> SELECTORS = Map.of(
            "username", "#username",
            "password", "#password",
            "green", "#btn-green",
            "red", "#btn-red",
            "logout", "#logout");

    private final AppUserService appUserService;
    private final SiteRepository sites;
    private final SiteCredentialRepository credentials;
    private final SecretStore secretStore;

    public LocalSeedRunner(AppUserService appUserService,
                           SiteRepository sites,
                           SiteCredentialRepository credentials,
                           SecretStore secretStore) {
        this.appUserService = appUserService;
        this.sites = sites;
        this.credentials = credentials;
        this.secretStore = secretStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    @Transactional
    public void seed() {
        AppUser demo = appUserService.getOrProvision(
                LocalCurrentUserService.DEMO_SUB, LocalCurrentUserService.DEMO_EMAIL, "Demo User", true);

        Site site = sites.findByBaseUrl(BASE_URL).orElseGet(() ->
                sites.save(new Site("Mock Target", BASE_URL, LOGIN_URL, SELECTORS, demo.getId())));

        if (credentials.findByOwnerId(demo.getId()).isEmpty()) {
            String secretRef = secretStore.store("supertrading/local/demo", "demopass");
            credentials.save(new SiteCredential(demo, site, "Local demo", "demo", secretRef));
            log.info("Seeded local demo user, mock site, and credential");
        } else {
            log.info("Local demo data already present; seed is a no-op");
        }
    }
}
