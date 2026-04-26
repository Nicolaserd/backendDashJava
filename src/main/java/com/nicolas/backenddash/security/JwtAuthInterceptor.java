package com.nicolas.backenddash.security;

import com.nicolas.backenddash.auth.JwtClaims;
import com.nicolas.backenddash.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

	private final JwtService jwtService;
	private final AuthContext authContext;

	public JwtAuthInterceptor(JwtService jwtService, AuthContext authContext) {
		this.jwtService = jwtService;
		this.authContext = authContext;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (isPublicRequest(request)) {
			return true;
		}

		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			throw new ResponseStatusException(UNAUTHORIZED, "Bearer token required");
		}

		JwtClaims claims = jwtService.validateToken(authorization.substring(7));
		authContext.setCurrentUser(new AuthenticatedUser(claims.usuarioId(), claims.email(), claims.rol(), claims.empresaId()));
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
		authContext.clear();
	}

	private boolean isPublicRequest(HttpServletRequest request) {
		String path = request.getRequestURI();
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
				|| path.equals("/error")
				|| path.equals("/swagger-ui.html")
				|| path.startsWith("/swagger-ui/")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/api/auth/");
	}
}
