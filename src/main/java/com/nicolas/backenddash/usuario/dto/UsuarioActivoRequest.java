package com.nicolas.backenddash.usuario.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioActivoRequest(
		@NotNull(message = "activo is required")
		Boolean activo
) {
}
