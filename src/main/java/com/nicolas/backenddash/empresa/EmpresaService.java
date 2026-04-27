package com.nicolas.backenddash.empresa;

import com.nicolas.backenddash.empresa.dto.EmpresaRequest;
import com.nicolas.backenddash.empresa.dto.EmpresaResponse;
import com.nicolas.backenddash.security.AuthorizationService;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import java.util.Comparator;
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
public class EmpresaService {

	private final EmpresaRepository empresaRepository;
	private final UsuarioRepository usuarioRepository;
	private final AuthorizationService authorizationService;

	public EmpresaService(
			EmpresaRepository empresaRepository,
			UsuarioRepository usuarioRepository,
			AuthorizationService authorizationService
	) {
		this.empresaRepository = empresaRepository;
		this.usuarioRepository = usuarioRepository;
		this.authorizationService = authorizationService;
	}

	@Transactional(readOnly = true)
	public List<EmpresaResponse> findAllPublic() {
		return empresaRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(Empresa::getNombre, String.CASE_INSENSITIVE_ORDER))
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public EmpresaResponse findById(UUID id) {
		authorizationService.requireAdmin();
		UUID currentEmpresaId = authorizationService.currentEmpresaId();
		if (currentEmpresaId != null && !currentEmpresaId.equals(id)) {
			throw new ResponseStatusException(FORBIDDEN, "Empresa access denied");
		}
		return toResponse(findEntity(id));
	}

	public EmpresaResponse create(EmpresaRequest request) {
		authorizationService.requireGlobalAdmin();
		if (isMotherName(request.nombre())) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard inc is reserved as mother company");
		}
		if (empresaRepository.existsByNombreIgnoreCase(request.nombre())) {
			throw new ResponseStatusException(CONFLICT, "Empresa name already exists");
		}
		Empresa empresa = new Empresa(request.nombre());
		return toResponse(empresaRepository.save(empresa));
	}

	public EmpresaResponse update(UUID id, EmpresaRequest request) {
		authorizationService.requireGlobalAdmin();
		Empresa empresa = findEntity(id);
		if (isMotherCompany(empresa)) {
			throw new ResponseStatusException(FORBIDDEN, "Mother company cannot be renamed");
		}
		if (isMotherName(request.nombre())) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard inc is reserved as mother company");
		}
		empresaRepository.findByNombreIgnoreCase(request.nombre()).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new ResponseStatusException(CONFLICT, "Empresa name already exists");
			}
		});
		empresa.setNombre(request.nombre());
		return toResponse(empresa);
	}

	public void delete(UUID id) {
		authorizationService.requireGlobalAdmin();
		Empresa empresa = findEntity(id);
		if (isMotherCompany(empresa)) {
			throw new ResponseStatusException(FORBIDDEN, "Mother company cannot be deleted");
		}
		if (usuarioRepository.countByEmpresaId(id) > 0) {
			throw new ResponseStatusException(CONFLICT, "Empresa has usuarios associated");
		}
		empresaRepository.delete(empresa);
	}

	public EmpresaResponse updateActiva(UUID id, Boolean activa) {
		authorizationService.requireAdmin();
		Empresa empresa = findEntity(id);
		UUID currentEmpresaId = authorizationService.currentEmpresaId();
		if (currentEmpresaId != null && !currentEmpresaId.equals(id)) {
			throw new ResponseStatusException(FORBIDDEN, "Admin can only change active status of their empresa");
		}
		empresa.setActiva(activa);
		return toResponse(empresa);
	}

	private Empresa findEntity(UUID id) {
		return empresaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Empresa not found"));
	}

	private EmpresaResponse toResponse(Empresa empresa) {
		long numeroEmpleados = usuarioRepository.countByEmpresaId(empresa.getId());
		if (empresa.getNumeroEmpleados() == null || empresa.getNumeroEmpleados() != (int) numeroEmpleados) {
			empresa.setNumeroEmpleados((int) numeroEmpleados);
		}
		return EmpresaResponse.from(empresa, numeroEmpleados);
	}

	private boolean isMotherName(String nombre) {
		return EmpresaRules.MOTHER_COMPANY_NAME.equalsIgnoreCase(nombre);
	}

	private boolean isMotherCompany(Empresa empresa) {
		return isMotherName(empresa.getNombre());
	}
}
