package com.nicolas.backenddash.auth;

import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import com.nicolas.backenddash.security.PasswordHashService;
import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioEstado;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import com.nicolas.backenddash.usuario.UsuarioRol;
import com.nicolas.backenddash.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@Transactional
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final EmpresaRepository empresaRepository;
	private final PasswordHashService passwordHashService;
	private final JwtService jwtService;

	public AuthService(
			UsuarioRepository usuarioRepository,
			EmpresaRepository empresaRepository,
			PasswordHashService passwordHashService,
			JwtService jwtService
	) {
		this.usuarioRepository = usuarioRepository;
		this.empresaRepository = empresaRepository;
		this.passwordHashService = passwordHashService;
		this.jwtService = jwtService;
	}

	public AuthResponse register(RegisterRequest request) {
		usuarioRepository.findByEmail(request.email()).ifPresent(usuario -> {
			throw new ResponseStatusException(CONFLICT, "Email already exists");
		});
		Empresa empresa = empresaRepository.findById(request.empresaId())
				.orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "empresaId must belong to an existing empresa"));

		Usuario usuario = new Usuario(
				request.nombre(),
				request.apellidos(),
				UsuarioRol.DASHBOARD_USUARIO,
				request.email(),
				empresa,
				passwordHashService.hash(request.password()),
				Boolean.TRUE,
				UsuarioEstado.NO_APROBADO
		);
		Usuario savedUsuario = usuarioRepository.save(usuario);
		return createAuthResponse(savedUsuario);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		Usuario usuario = usuarioRepository.findByEmail(request.email())
				.orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

		if (!Boolean.TRUE.equals(usuario.getActivo())
				|| !passwordHashService.matches(request.password(), usuario.getPasswordHash())) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
		}
		if (usuario.getEstado() != UsuarioEstado.APROBADO) {
			throw new ResponseStatusException(UNAUTHORIZED, "User is not approved yet");
		}
		if (usuario.getRol() != UsuarioRol.SUPER_ADMIN
				&& usuario.getEmpresa() != null
				&& !Boolean.TRUE.equals(usuario.getEmpresa().getActiva())) {
			throw new ResponseStatusException(FORBIDDEN, "Empresa is inactive");
		}

		return createAuthResponse(usuario);
	}

	private AuthResponse createAuthResponse(Usuario usuario) {
		return new AuthResponse(
				jwtService.generateToken(usuario),
				"Bearer",
				jwtService.getExpirationSeconds(),
				UsuarioResponse.from(usuario)
		);
	}
}
