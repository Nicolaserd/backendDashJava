package com.nicolas.backenddash.usuario;

import com.nicolas.backenddash.usuario.dto.UsuarioRequest;
import com.nicolas.backenddash.usuario.dto.UsuarioResponse;
import com.nicolas.backenddash.usuario.dto.UsuarioActivoRequest;
import com.nicolas.backenddash.usuario.dto.UsuarioEstadoRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administracion de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	@Operation(summary = "Listar usuarios", description = "Solo usuarios ADMIN.")
	public List<UsuarioResponse> findAll() {
		return usuarioService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar usuario", description = "Solo usuarios ADMIN.")
	public UsuarioResponse findById(@PathVariable UUID id) {
		return usuarioService.findById(id);
	}

	@PostMapping
	@Operation(summary = "Crear usuario", description = "Solo ADMIN. Permite crear admins, creadores y consumidores.")
	public ResponseEntity<UsuarioResponse> create(@Valid @RequestBody UsuarioRequest request) {
		UsuarioResponse response = usuarioService.create(request);
		return ResponseEntity
				.created(URI.create("/api/usuarios/" + response.id()))
				.body(response);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar usuario", description = "Solo usuarios ADMIN.")
	public UsuarioResponse update(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest request) {
		return usuarioService.update(id, request);
	}

	@PatchMapping("/{id}/estado")
	@Operation(summary = "Aprobar o no aprobar usuario", description = "Solo ADMIN de la misma empresa o SUPER_ADMIN.")
	public UsuarioResponse updateEstado(@PathVariable UUID id, @Valid @RequestBody UsuarioEstadoRequest request) {
		return usuarioService.updateEstado(id, request.estado());
	}

	@PatchMapping("/{id}/activo")
	@Operation(summary = "Activar o desactivar usuario", description = "Solo ADMIN de la misma empresa o SUPER_ADMIN.")
	public UsuarioResponse updateActivo(@PathVariable UUID id, @Valid @RequestBody UsuarioActivoRequest request) {
		return usuarioService.updateActivo(id, request.activo());
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar usuario", description = "Solo usuarios ADMIN.")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		usuarioService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
