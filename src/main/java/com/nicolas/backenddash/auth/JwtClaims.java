package com.nicolas.backenddash.auth;

import com.nicolas.backenddash.usuario.UsuarioRol;
import java.util.UUID;

public record JwtClaims(
		UUID usuarioId,
		String email,
		UsuarioRol rol,
		UUID empresaId
) {
}
