package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.exception.ResourceNotFoundException;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Site;
import com.rick.supertrading.domain.model.SiteCredential;
import com.rick.supertrading.domain.port.SecretStore;
import com.rick.supertrading.domain.repository.SiteCredentialRepository;
import com.rick.supertrading.domain.repository.SiteRepository;
import com.rick.supertrading.domain.service.dto.CreateCredentialCommand;
import com.rick.supertrading.domain.service.dto.CredentialView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Manages bot credentials. Creation stores the external password in the
 * {@link SecretStore} and persists only the returned reference. All reads are
 * ownership-scoped; ADMINs see everything.
 */
@Service
public class SiteCredentialService {

    private final SiteCredentialRepository credentials;
    private final SiteRepository sites;
    private final SecretStore secretStore;
    private final AuditService audit;

    public SiteCredentialService(SiteCredentialRepository credentials,
                                 SiteRepository sites,
                                 SecretStore secretStore,
                                 AuditService audit) {
        this.credentials = credentials;
        this.sites = sites;
        this.secretStore = secretStore;
        this.audit = audit;
    }

    @Transactional
    public CredentialView create(AppUser owner, CreateCredentialCommand cmd) {
        Site site = sites.findById(cmd.siteId())
                .orElseThrow(() -> new ResourceNotFoundException("site", cmd.siteId()));

        String secretName = "supertrading/cred/%d/%d/%s".formatted(owner.getId(), site.getId(), cmd.username());
        String secretRef = secretStore.store(secretName, cmd.password());

        SiteCredential credential = credentials.save(
                new SiteCredential(owner, site, cmd.label(), cmd.username(), secretRef));
        audit.record(owner.getId(), "site_credential", credential.getId(), "CREATE",
                Map.of("siteId", site.getId(), "username", cmd.username()));
        return CredentialView.from(credential);
    }

    @Transactional(readOnly = true)
    public List<CredentialView> list(AppUser user) {
        List<SiteCredential> result = user.isAdmin()
                ? credentials.findAll()
                : credentials.findByOwnerId(user.getId());
        return result.stream().map(CredentialView::from).toList();
    }

    @Transactional(readOnly = true)
    public CredentialView get(AppUser user, Long id) {
        return CredentialView.from(requireOwned(user, id));
    }

    private SiteCredential requireOwned(AppUser user, Long id) {
        return (user.isAdmin() ? credentials.findById(id) : credentials.findByIdAndOwnerId(id, user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("credential", id));
    }
}
