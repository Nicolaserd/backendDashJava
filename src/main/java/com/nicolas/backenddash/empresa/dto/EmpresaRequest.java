package com.nicolas.backenddash.empresa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
		@NotBlank(message = "nombre is required")
		@Size(max = 160, message = "nombre max length is 160")
		String nombre
) {
}
