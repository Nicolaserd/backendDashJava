package com.nicolas.backenddash.dashboard.dto;

import com.nicolas.backenddash.dashboard.Dashboard;
import com.nicolas.backenddash.dashboard.DashboardType;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record DashboardResponse(
		UUID id,
		String nombre,
		DashboardType tipo,
		String contenido,
		UUID creadorId,
		UUID empresaId,
		Set<UUID> usuariosAsignadosIds
) {
	public static DashboardResponse from(Dashboard dashboard) {
		return new DashboardResponse(
				dashboard.getId(),
				dashboard.getNombre(),
				dashboard.getTipo(),
				dashboard.getContenido(),
				dashboard.getCreador() != null ? dashboard.getCreador().getId() : null,
				dashboard.getEmpresa() != null ? dashboard.getEmpresa().getId() : null,
				dashboard.getUsuariosAsignados()
						.stream()
						.map(usuario -> usuario.getId())
						.collect(Collectors.toSet())
		);
	}
}
