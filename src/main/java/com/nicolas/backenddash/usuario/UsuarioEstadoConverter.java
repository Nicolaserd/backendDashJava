package com.nicolas.backenddash.usuario;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = true)
public class UsuarioEstadoConverter implements AttributeConverter<UsuarioEstado, String> {

	@Override
	public String convertToDatabaseColumn(UsuarioEstado attribute) {
		return attribute != null ? attribute.name() : null;
	}

	@Override
	public UsuarioEstado convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		String normalized = dbData
				.trim()
				.toUpperCase(Locale.ROOT)
				.replace('-', '_')
				.replace(' ', '_');

		return switch (normalized) {
			case "APROBADO", "APROVADO" -> UsuarioEstado.APROBADO;
			case "NO_APROBADO", "NO_APROVADO", "NOAPROBADO", "NOAPROVADO" -> UsuarioEstado.NO_APROBADO;
			default -> throw new IllegalArgumentException("Invalid usuario estado value");
		};
	}
}
