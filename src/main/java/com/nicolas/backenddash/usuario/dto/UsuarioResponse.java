package com.nicolas.backenddash.usuario.dto;

import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioEstado;
import com.nicolas.backenddash.usuario.UsuarioRol;
import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
		UUID id,
		String nombre,
		String apellidos,
		UsuarioRol rol,
		UsuarioEstado estado,
		String email,
		Boolean activo,
		UUID empresaId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static UsuarioResponse from(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getApellidos(),
				usuario.getRol(),
				usuario.getEstado(),
				usuario.getEmail(),
				usuario.getActivo(),
				usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null,
				usuario.getCreatedAt(),
				usuario.getUpdatedAt()
		);
	}
}
