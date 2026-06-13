package com.rick.supertrading.web.dto;

import com.rick.supertrading.domain.service.dto.CreateCredentialCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCredentialRequest(
        @NotNull Long siteId,
        String label,
        @NotBlank String username,
        @NotBlank String password
) {
    public CreateCredentialCommand toCommand() {
        return new CreateCredentialCommand(siteId, label, username, password);
    }
}
