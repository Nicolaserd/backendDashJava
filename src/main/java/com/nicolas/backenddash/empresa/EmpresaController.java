package com.nicolas.backenddash.empresa;

import com.nicolas.backenddash.empresa.dto.EmpresaRequest;
import com.nicolas.backenddash.empresa.dto.EmpresaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Administracion de empresas")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

	private final EmpresaService empresaService;

	public EmpresaController(EmpresaService empresaService) {
		this.empresaService = empresaService;
	}

	@GetMapping
	@Operation(summary = "Listar empresas", description = "Solo usuarios ADMIN.")
	public List<EmpresaResponse> findAll() {
		return empresaService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar empresa", description = "Solo usuarios ADMIN.")
	public EmpresaResponse findById(@PathVariable UUID id) {
		return empresaService.findById(id);
	}

	@PostMapping
	@Operation(summary = "Crear empresa", description = "Solo usuarios ADMIN.")
	public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest request) {
		EmpresaResponse response = empresaService.create(request);
		return ResponseEntity
				.created(URI.create("/api/empresas/" + response.id()))
				.body(response);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar empresa", description = "Solo usuarios ADMIN.")
	public EmpresaResponse update(@PathVariable UUID id, @Valid @RequestBody EmpresaRequest request) {
		return empresaService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar empresa", description = "Solo usuarios ADMIN.")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		empresaService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
