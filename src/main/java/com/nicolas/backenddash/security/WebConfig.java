package com.nicolas.backenddash.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final JwtAuthInterceptor jwtAuthInterceptor;
	private final String[] allowedOriginPatterns;

	public WebConfig(
			JwtAuthInterceptor jwtAuthInterceptor,
			@Value("${app.cors.allowed-origin-patterns:*}") String[] allowedOriginPatterns
	) {
		this.jwtAuthInterceptor = jwtAuthInterceptor;
		this.allowedOriginPatterns = allowedOriginPatterns;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtAuthInterceptor)
				.addPathPatterns("/api/**");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOriginPatterns(allowedOriginPatterns)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.exposedHeaders("Authorization")
				.allowCredentials(false)
				.maxAge(3600);
	}
}
