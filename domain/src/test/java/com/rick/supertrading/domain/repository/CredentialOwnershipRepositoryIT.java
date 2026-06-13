package com.rick.supertrading.domain.repository;

import com.rick.supertrading.domain.DomainTestApp;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Role;
import com.rick.supertrading.domain.model.Site;
import com.rick.supertrading.domain.model.SiteCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the multi-user ownership model, the {@code (owner, site, username)}
 * unique constraint, and the JSONB selector mapping against a real PostgreSQL
 * instance with the Flyway schema applied (Hibernate runs in validate mode).
 *
 * <p>Skipped automatically when Docker is not available (e.g. local dev without
 * a daemon); runs fully in CI.
 */
@SpringBootTest(classes = DomainTestApp.class)
@EnabledIf("dockerAvailable")
class CredentialOwnershipRepositoryIT {

    static PostgreSQLContainer<?> postgres;

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Flyway owns the schema; Hibernate only validates its mappings against it.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    private final AppUserRepository users;
    private final SiteRepository sites;
    private final SiteCredentialRepository credentials;

    CredentialOwnershipRepositoryIT(AppUserRepository users,
                                    SiteRepository sites,
                                    SiteCredentialRepository credentials) {
        this.users = users;
        this.sites = sites;
        this.credentials = credentials;
    }

    @Test
    void ownershipFilterIsolatesUsers() {
        AppUser alice = users.save(new AppUser("alice@example.com", "Alice", "sub-alice", Role.USER));
        AppUser bob = users.save(new AppUser("bob@example.com", "Bob", "sub-bob", Role.USER));
        Site site = sites.save(newSite("https://target.example.com"));

        SiteCredential aliceCred = credentials.save(
                new SiteCredential(alice, site, "alice work", "alice-login", "arn:aws:secret:alice"));
        credentials.save(new SiteCredential(bob, site, "bob work", "bob-login", "arn:aws:secret:bob"));

        assertThat(credentials.findByOwnerId(alice.getId()))
                .extracting(SiteCredential::getId)
                .containsExactly(aliceCred.getId());
        assertThat(credentials.findByIdAndOwnerId(aliceCred.getId(), bob.getId())).isEmpty();
        assertThat(credentials.findByIdAndOwnerId(aliceCred.getId(), alice.getId())).isPresent();
    }

    @Test
    void sameUserMayHoldMultipleLoginsOnOneSiteButNotDuplicateUsername() {
        AppUser user = users.save(new AppUser("carol@example.com", "Carol", "sub-carol", Role.USER));
        Site site = sites.save(newSite("https://shared.example.com"));

        credentials.saveAndFlush(new SiteCredential(user, site, "primary", "login-1", "arn:1"));
        credentials.saveAndFlush(new SiteCredential(user, site, "secondary", "login-2", "arn:2"));

        assertThat(credentials.findByOwnerId(user.getId())).hasSize(2);

        assertThatThrownBy(() ->
                credentials.saveAndFlush(new SiteCredential(user, site, "dup", "login-1", "arn:3")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void selectorsRoundTripAsJsonb() {
        Site saved = sites.saveAndFlush(newSite("https://json.example.com"));
        Site reloaded = sites.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getSelectors())
                .containsEntry("green", ".btn-green")
                .containsEntry("red", ".btn-red");
    }

    private static Site newSite(String baseUrl) {
        return new Site(
                "Target",
                baseUrl,
                baseUrl + "/login",
                Map.of(
                        "username", "#u",
                        "password", "#p",
                        "green", ".btn-green",
                        "red", ".btn-red",
                        "logout", "#out"
                ),
                null);
    }
}
