package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.exception.ResourceNotFoundException;
import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Site;
import com.rick.supertrading.domain.repository.SiteRepository;
import com.rick.supertrading.domain.service.dto.CreateSiteCommand;
import com.rick.supertrading.domain.service.dto.SiteView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Manages the shared {@link Site} catalog. The catalog is not user-owned: any
 * authenticated user may register or browse target definitions.
 */
@Service
public class SiteService {

    private final SiteRepository repository;
    private final AuditService audit;

    public SiteService(SiteRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
    }

    @Transactional
    public SiteView create(AppUser actor, CreateSiteCommand cmd) {
        if (repository.existsByBaseUrl(cmd.baseUrl())) {
            throw new IllegalArgumentException("A site with base_url already exists: " + cmd.baseUrl());
        }
        Site site = repository.save(
                new Site(cmd.name(), cmd.baseUrl(), cmd.loginUrl(), cmd.selectors(), actor.getId()));
        audit.record(actor.getId(), "site", site.getId(), "CREATE",
                Map.of("baseUrl", cmd.baseUrl(), "name", cmd.name()));
        return SiteView.from(site);
    }

    @Transactional(readOnly = true)
    public List<SiteView> list() {
        return repository.findAll().stream().map(SiteView::from).toList();
    }

    @Transactional(readOnly = true)
    public SiteView get(Long id) {
        return repository.findById(id).map(SiteView::from)
                .orElseThrow(() -> new ResourceNotFoundException("site", id));
    }
}
