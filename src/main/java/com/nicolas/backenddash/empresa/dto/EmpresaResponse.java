package com.nicolas.backenddash.empresa.dto;

import com.nicolas.backenddash.empresa.Empresa;
import java.util.UUID;

public record EmpresaResponse(
		UUID id,
		String nombre,
		long numeroEmpleados
) {
	public static EmpresaResponse from(Empresa empresa, long numeroEmpleados) {
		return new EmpresaResponse(
				empresa.getId(),
				empresa.getNombre(),
				numeroEmpleados
		);
	}
}
