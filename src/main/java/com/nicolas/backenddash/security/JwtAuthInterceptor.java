package com.nicolas.backenddash.security;

import com.nicolas.backenddash.auth.JwtClaims;
import com.nicolas.backenddash.auth.JwtService;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioEstado;
import com.nicolas.backenddash.usuario.UsuarioRol;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

	private final JwtService jwtService;
	private final AuthContext authContext;
	private final UsuarioRepository usuarioRepository;
	private final EmpresaRepository empresaRepository;

	public JwtAuthInterceptor(
			JwtService jwtService,
			AuthContext authContext,
			UsuarioRepository usuarioRepository,
			EmpresaRepository empresaRepository
	) {
		this.jwtService = jwtService;
		this.authContext = authContext;
		this.usuarioRepository = usuarioRepository;
		this.empresaRepository = empresaRepository;
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
		Usuario usuario = usuarioRepository.findById(claims.usuarioId())
				.orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid token user"));
		if (!Boolean.TRUE.equals(usuario.getActivo())) {
			throw new ResponseStatusException(FORBIDDEN, "User is inactive");
		}
		if (usuario.getEstado() != UsuarioEstado.APROBADO) {
			throw new ResponseStatusException(FORBIDDEN, "User is not approved yet");
		}
		if (usuario.getRol() != UsuarioRol.SUPER_ADMIN
				&& usuario.getEmpresa() != null
				&& !empresaRepository.existsByIdAndActivaTrue(usuario.getEmpresa().getId())) {
			throw new ResponseStatusException(FORBIDDEN, "Empresa is inactive");
		}

		authContext.setCurrentUser(new AuthenticatedUser(
				usuario.getId(),
				usuario.getEmail(),
				usuario.getRol(),
				usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null
		));
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
				|| ("GET".equalsIgnoreCase(request.getMethod()) && (path.equals("/api/empresas") || path.equals("/api/empresas/")))
				|| path.startsWith("/api/auth/");
	}
}
