package com.rick.supertrading.web.dto;

import com.rick.supertrading.domain.service.dto.CreateSiteCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record CreateSiteRequest(
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank String loginUrl,
        @NotEmpty Map<String, String> selectors
) {
    public CreateSiteCommand toCommand() {
        return new CreateSiteCommand(name, baseUrl, loginUrl, selectors);
    }
}
