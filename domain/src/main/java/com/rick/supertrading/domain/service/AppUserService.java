package com.rick.supertrading.domain.service;

import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.model.Role;
import com.rick.supertrading.domain.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Resolves and just-in-time provisions console users from their Cognito identity.
 * On first login a row is created; on subsequent logins the role and profile are
 * reconciled with the IdP token (so promoting a user to ADMIN in Cognito takes
 * effect on their next request).
 */
@Service
public class AppUserService {

    private final AppUserRepository repository;

    public AppUserService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AppUser getOrProvision(String externalIdpSub, String email, String displayName, boolean admin) {
        Role desiredRole = admin ? Role.ADMIN : Role.USER;
        return repository.findByExternalIdpSub(externalIdpSub)
                .map(existing -> reconcile(existing, email, displayName, desiredRole))
                .orElseGet(() -> repository.save(new AppUser(email, displayName, externalIdpSub, desiredRole)));
    }

    private AppUser reconcile(AppUser user, String email, String displayName, Role desiredRole) {
        if (user.getRole() != desiredRole) {
            user.setRole(desiredRole);
        }
        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
        }
        if (displayName != null && !displayName.equals(user.getDisplayName())) {
            user.setDisplayName(displayName);
        }
        return user; // managed entity; flushed on commit
    }

    /** ADMIN-only listing of all console users. */
    @Transactional(readOnly = true)
    public List<AppUser> findAll() {
        return repository.findAll();
    }
}
