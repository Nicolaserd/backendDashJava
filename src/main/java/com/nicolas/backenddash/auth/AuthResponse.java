package com.nicolas.backenddash.auth;

import com.nicolas.backenddash.usuario.dto.UsuarioResponse;

public record AuthResponse(
		String token,
		String tokenType,
		long expiresIn,
		UsuarioResponse usuario
) {
}
