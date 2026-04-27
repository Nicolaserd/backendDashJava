package com.nicolas.backenddash.usuario;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = true)
public class UsuarioRolConverter implements AttributeConverter<UsuarioRol, String> {

	@Override
	public String convertToDatabaseColumn(UsuarioRol attribute) {
		return attribute != null ? attribute.name() : null;
	}

	@Override
	public UsuarioRol convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		String normalized = dbData
				.trim()
				.toUpperCase(Locale.ROOT)
				.replace('-', '_')
				.replace(' ', '_');

		return switch (normalized) {
			case "SUPER_ADMIN", "SUPERADMIN" -> UsuarioRol.SUPER_ADMIN;
			case "ADMIN" -> UsuarioRol.ADMIN;
			case "DASHBOARD_CREADOR", "CREADOR_DE_DASHBOARD", "CREADOR_DASHBOARD", "DASHBOARDCREADOR" ->
					UsuarioRol.DASHBOARD_CREADOR;
			case "DASHBOARD_USUARIO", "DASHBOARD_CONSUMIDOR", "CONSUMIDOR_DE_DASHBOARD", "CONSUMIDOR_DASHBOARD" ->
					UsuarioRol.DASHBOARD_USUARIO;
			default -> throw new IllegalArgumentException("Invalid usuario rol value");
		};
	}
}
