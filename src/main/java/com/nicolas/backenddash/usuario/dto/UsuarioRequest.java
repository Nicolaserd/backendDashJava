package com.nicolas.backenddash.usuario.dto;

import com.nicolas.backenddash.usuario.UsuarioRol;
import com.nicolas.backenddash.usuario.UsuarioEstado;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UsuarioRequest(
		@NotBlank(message = "nombre is required")
		@Size(max = 80, message = "nombre max length is 80")
		String nombre,

		@NotBlank(message = "apellidos is required")
		@Size(max = 120, message = "apellidos max length is 120")
		String apellidos,

		@NotNull(message = "rol is required")
		UsuarioRol rol,

		@NotBlank(message = "email is required")
		@Email(message = "email format is invalid")
		@Size(max = 180, message = "email max length is 180")
		String email,

		@NotBlank(message = "password is required")
		@Size(min = 8, max = 255, message = "password length must be between 8 and 255")
		String password,

		Boolean activo,

		UsuarioEstado estado,

		UUID empresaId
) {
}
