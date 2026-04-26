package com.nicolas.backenddash.auth;

import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import com.nicolas.backenddash.security.PasswordHashService;
import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import com.nicolas.backenddash.usuario.UsuarioRol;
import com.nicolas.backenddash.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
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
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Empresa not found"));

		Usuario usuario = new Usuario(
				request.nombre(),
				request.apellidos(),
				UsuarioRol.DASHBOARD_USUARIO,
				request.email(),
				empresa,
				passwordHashService.hash(request.password()),
				Boolean.TRUE
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
