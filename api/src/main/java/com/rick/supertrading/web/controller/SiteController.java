package com.rick.supertrading.web.controller;

import com.rick.supertrading.domain.service.SiteService;
import com.rick.supertrading.domain.service.dto.SiteView;
import com.rick.supertrading.security.CurrentUser;
import com.rick.supertrading.web.dto.CreateSiteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catalog of target sites (shared, not user-owned). Spec §6.3. */
@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;
    private final CurrentUser currentUser;

    public SiteController(SiteService siteService, CurrentUser currentUser) {
        this.siteService = siteService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<SiteView> create(@Valid @RequestBody CreateSiteRequest request) {
        SiteView created = siteService.create(currentUser.require(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<SiteView> list() {
        return siteService.list();
    }

    @GetMapping("/{id}")
    public SiteView get(@PathVariable Long id) {
        return siteService.get(id);
    }
}
