package com.nicolas.backenddash.dashboard.dto;

import com.nicolas.backenddash.dashboard.DashboardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DashboardRequest(
		@NotBlank(message = "nombre is required")
		@Size(max = 120, message = "nombre max length is 120")
		String nombre,

		@NotNull(message = "tipo is required")
		DashboardType tipo,

		@NotBlank(message = "contenido is required")
		String contenido,

		UUID creadorId,

		UUID empresaId
) {
}
