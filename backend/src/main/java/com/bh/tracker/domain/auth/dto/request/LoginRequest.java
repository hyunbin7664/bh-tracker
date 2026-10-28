package com.bh.tracker.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull Long organizationId, @NotBlank String password) {
}
