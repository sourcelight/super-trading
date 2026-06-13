package com.rick.supertrading.domain.service.dto;

import java.util.Map;

/** Inputs to register a catalog site. */
public record CreateSiteCommand(
        String name,
        String baseUrl,
        String loginUrl,
        Map<String, String> selectors
) {
}
