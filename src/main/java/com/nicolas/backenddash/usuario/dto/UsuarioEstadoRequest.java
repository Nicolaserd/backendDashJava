package com.nicolas.backenddash.usuario.dto;

import com.nicolas.backenddash.usuario.UsuarioEstado;
import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoRequest(
		@NotNull(message = "estado is required")
		UsuarioEstado estado
) {
}
