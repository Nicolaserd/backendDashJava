package com.nicolas.backenddash.dashboard.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DashboardAssignmentRequest(
		@NotNull(message = "usuarioId is required")
		UUID usuarioId
) {
}
