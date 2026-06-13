package com.rick.supertrading.security;

import com.rick.supertrading.domain.model.AppUser;

/**
 * Resolves the {@link AppUser} for the current request — the single bridge between the
 * web-security principal and the domain ownership model. Has a JWT-backed implementation
 * for the cloud profiles and a fixed demo-user implementation for the {@code local} profile.
 */
public interface CurrentUser {

    AppUser require();
}
