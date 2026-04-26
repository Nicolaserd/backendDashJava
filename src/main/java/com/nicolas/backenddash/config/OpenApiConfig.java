package com.nicolas.backenddash.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI backendDashOpenApi() {
		String bearerScheme = "bearerAuth";
		return new OpenAPI()
				.info(new Info()
						.title("Backend Dash API")
						.version("1.0.0")
						.description("API para autenticacion, empresas, usuarios y dashboards con permisos por rol."))
				.addSecurityItem(new SecurityRequirement().addList(bearerScheme))
				.components(new Components()
						.addSecuritySchemes(bearerScheme, new SecurityScheme()
								.name(bearerScheme)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
