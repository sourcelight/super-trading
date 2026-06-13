package com.rick.supertrading.web.controller;

import com.rick.supertrading.domain.service.SiteCredentialService;
import com.rick.supertrading.domain.service.dto.CredentialView;
import com.rick.supertrading.security.CurrentUser;
import com.rick.supertrading.web.dto.CreateCredentialRequest;
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

/**
 * Bot credentials owned by the current user. The password is sent once on create,
 * stored in the secret store, and never returned (only its reference). Spec §6.3.
 */
@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    private final SiteCredentialService credentialService;
    private final CurrentUser currentUser;

    public CredentialController(SiteCredentialService credentialService, CurrentUser currentUser) {
        this.credentialService = credentialService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<CredentialView> create(@Valid @RequestBody CreateCredentialRequest request) {
        CredentialView created = credentialService.create(currentUser.require(), request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<CredentialView> list() {
        return credentialService.list(currentUser.require());
    }

    @GetMapping("/{id}")
    public CredentialView get(@PathVariable Long id) {
        return credentialService.get(currentUser.require(), id);
    }
}
