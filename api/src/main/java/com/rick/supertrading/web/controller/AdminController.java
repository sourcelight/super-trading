package com.rick.supertrading.web.controller;

import com.rick.supertrading.domain.model.AppUser;
import com.rick.supertrading.domain.service.AppUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ADMIN-only console area (spec §10 screen 7). Protected both by the URL rule on
 * {@code /api/admin/**} and by method security as defence in depth.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AppUserService appUserService;

    public AdminController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/users")
    public List<AppUser> users() {
        return appUserService.findAll();
    }
}
