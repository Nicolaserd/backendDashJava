package com.nicolas.backenddash.security;

import com.nicolas.backenddash.usuario.UsuarioRol;
import java.util.UUID;

public record AuthenticatedUser(
		UUID id,
		String email,
		UsuarioRol rol,
		UUID empresaId
) {
	public boolean isSuperAdmin() {
		return rol == UsuarioRol.SUPER_ADMIN;
	}

	public boolean isAdmin() {
		return rol == UsuarioRol.ADMIN || rol == UsuarioRol.SUPER_ADMIN;
	}

	public boolean isDashboardCreator() {
		return rol == UsuarioRol.DASHBOARD_CREADOR;
	}

	public boolean isDashboardConsumer() {
		return rol == UsuarioRol.DASHBOARD_USUARIO;
	}
}
