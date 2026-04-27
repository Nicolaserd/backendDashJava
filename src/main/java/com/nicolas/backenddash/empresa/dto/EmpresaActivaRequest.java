package com.nicolas.backenddash.empresa.dto;

import jakarta.validation.constraints.NotNull;

public record EmpresaActivaRequest(
		@NotNull(message = "activa is required")
		Boolean activa
) {
}
