package com.nicolas.backenddash.usuario;

import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import com.nicolas.backenddash.empresa.EmpresaRules;
import com.nicolas.backenddash.usuario.dto.UsuarioRequest;
import com.nicolas.backenddash.usuario.dto.UsuarioResponse;
import com.nicolas.backenddash.security.PasswordHashService;
import com.nicolas.backenddash.security.AuthorizationService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final EmpresaRepository empresaRepository;
	private final PasswordHashService passwordHashService;
	private final AuthorizationService authorizationService;

	public UsuarioService(
			UsuarioRepository usuarioRepository,
			EmpresaRepository empresaRepository,
			PasswordHashService passwordHashService,
			AuthorizationService authorizationService
	) {
		this.usuarioRepository = usuarioRepository;
		this.empresaRepository = empresaRepository;
		this.passwordHashService = passwordHashService;
		this.authorizationService = authorizationService;
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> findAll() {
		authorizationService.requireAdmin();
		UUID adminEmpresaId = authorizationService.currentEmpresaId();
		List<Usuario> usuarios = adminEmpresaId == null
				? usuarioRepository.findAll()
				: usuarioRepository.findByEmpresaId(adminEmpresaId);
		return usuarios
				.stream()
				.map(UsuarioResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public UsuarioResponse findById(UUID id) {
		authorizationService.requireAdmin();
		Usuario usuario = findEntity(id);
		ensureAdminCanAccess(usuario);
		return UsuarioResponse.from(usuario);
	}

	public UsuarioResponse create(UsuarioRequest request) {
		authorizationService.requireAdmin();
		validateRoleAssignment(request.rol());
		validateUniqueEmail(request.email(), null);
		Empresa empresa = resolveEmpresaForWrite(request.empresaId(), request.rol());
		Usuario usuario = new Usuario(
				request.nombre(),
				request.apellidos(),
				request.rol(),
				request.email(),
				empresa,
				passwordHashService.hash(request.password()),
				request.activo()
		);
		return UsuarioResponse.from(usuarioRepository.save(usuario));
	}

	public UsuarioResponse update(UUID id, UsuarioRequest request) {
		authorizationService.requireAdmin();
		Usuario usuario = findEntity(id);
		validateRoleAssignment(request.rol());
		validateUniqueEmail(request.email(), id);
		usuario.setNombre(request.nombre());
		usuario.setApellidos(request.apellidos());
		usuario.setRol(request.rol());
		usuario.setEmail(request.email());
		ensureAdminCanAccess(usuario);
		usuario.setEmpresa(resolveEmpresaForWrite(request.empresaId(), request.rol()));
		usuario.setPasswordHash(passwordHashService.hash(request.password()));
		usuario.setActivo(request.activo() != null ? request.activo() : Boolean.TRUE);
		return UsuarioResponse.from(usuario);
	}

	public void delete(UUID id) {
		authorizationService.requireAdmin();
		Usuario usuario = findEntity(id);
		ensureAdminCanAccess(usuario);
		usuarioRepository.delete(usuario);
	}

	private Usuario findEntity(UUID id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario not found"));
	}

	private void validateUniqueEmail(String email, UUID currentId) {
		usuarioRepository.findByEmail(email).ifPresent(existing -> {
			if (!existing.getId().equals(currentId)) {
				throw new ResponseStatusException(CONFLICT, "Email already exists");
			}
		});
	}

	private Empresa resolveEmpresa(UUID empresaId) {
		if (empresaId == null) {
			return null;
		}
		return empresaRepository.findById(empresaId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Empresa not found"));
	}

	private Empresa resolveEmpresaForWrite(UUID requestedEmpresaId, UsuarioRol requestedRole) {
		if (requestedRole == UsuarioRol.SUPER_ADMIN) {
			return findOrCreateMotherCompany();
		}

		UUID adminEmpresaId = authorizationService.currentEmpresaId();
		if (adminEmpresaId == null) {
			if (requestedEmpresaId == null) {
				throw new ResponseStatusException(FORBIDDEN, "empresaId is required for non-super-admin users");
			}
			return resolveEmpresa(requestedEmpresaId);
		}
		if (requestedEmpresaId != null && !requestedEmpresaId.equals(adminEmpresaId)) {
			throw new ResponseStatusException(FORBIDDEN, "Admin can only manage usuarios from their empresa");
		}
		return resolveEmpresa(adminEmpresaId);
	}

	private void ensureAdminCanAccess(Usuario usuario) {
		UUID adminEmpresaId = authorizationService.currentEmpresaId();
		if (adminEmpresaId == null) {
			return;
		}
		if (usuario.getEmpresa() == null || !adminEmpresaId.equals(usuario.getEmpresa().getId())) {
			throw new ResponseStatusException(FORBIDDEN, "Usuario does not belong to your empresa");
		}
	}

	private void validateRoleAssignment(UsuarioRol requestedRole) {
		if (requestedRole == UsuarioRol.SUPER_ADMIN && !authorizationService.currentUser().isSuperAdmin()) {
			throw new ResponseStatusException(FORBIDDEN, "Only SUPER_ADMIN can assign SUPER_ADMIN role");
		}
	}

	private Empresa findOrCreateMotherCompany() {
		return empresaRepository.findByNombreIgnoreCase(EmpresaRules.MOTHER_COMPANY_NAME)
				.orElseGet(() -> empresaRepository.save(new Empresa(EmpresaRules.MOTHER_COMPANY_NAME)));
	}

}
