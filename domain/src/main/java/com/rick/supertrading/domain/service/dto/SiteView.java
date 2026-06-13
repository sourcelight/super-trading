package com.rick.supertrading.domain.service.dto;

import com.rick.supertrading.domain.model.Site;

import java.time.Instant;
import java.util.Map;

/** Read model for a catalog {@link Site}. */
public record SiteView(
        Long id,
        String name,
        String baseUrl,
        String loginUrl,
        Map<String, String> selectors,
        Long createdBy,
        Instant createdAt
) {
    public static SiteView from(Site s) {
        return new SiteView(s.getId(), s.getName(), s.getBaseUrl(), s.getLoginUrl(),
                s.getSelectors(), s.getCreatedBy(), s.getCreatedAt());
    }
}
