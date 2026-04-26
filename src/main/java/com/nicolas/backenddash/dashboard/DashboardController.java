package com.nicolas.backenddash.dashboard;

import com.nicolas.backenddash.dashboard.dto.DashboardAssignmentRequest;
import com.nicolas.backenddash.dashboard.dto.DashboardRequest;
import com.nicolas.backenddash.dashboard.dto.DashboardResponse;
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
@RequestMapping("/api/dashboards")
@Tag(name = "Dashboards", description = "Gestion y asignacion de dashboards")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping
	@Operation(summary = "Listar dashboards", description = "Admin ve todos, creador ve los propios y consumidor ve los asignados.")
	public List<DashboardResponse> findAll() {
		return dashboardService.findAll();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consultar dashboard", description = "Respeta permisos por rol y asignacion.")
	public DashboardResponse findById(@PathVariable UUID id) {
		return dashboardService.findById(id);
	}

	@PostMapping
	@Operation(summary = "Crear dashboard", description = "Admin o creador pueden crear. El creador queda asociado al dashboard.")
	public ResponseEntity<DashboardResponse> create(@Valid @RequestBody DashboardRequest request) {
		DashboardResponse response = dashboardService.create(request);
		return ResponseEntity
				.created(URI.create("/api/dashboards/" + response.id()))
				.body(response);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar dashboard", description = "Admin puede actualizar cualquiera. Creador solo actualiza dashboards propios.")
	public DashboardResponse update(@PathVariable UUID id, @Valid @RequestBody DashboardRequest request) {
		return dashboardService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar dashboard", description = "Admin puede eliminar cualquiera. Creador solo elimina dashboards propios.")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		dashboardService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/usuarios")
	@Operation(summary = "Asignar consumidor", description = "Admin o creador propietario asignan un usuario consumidor para que pueda ver el dashboard.")
	public DashboardResponse assignUsuario(
			@PathVariable UUID id,
			@Valid @RequestBody DashboardAssignmentRequest request
	) {
		return dashboardService.assignUsuario(id, request.usuarioId());
	}

	@DeleteMapping("/{id}/usuarios/{usuarioId}")
	@Operation(summary = "Quitar consumidor", description = "Admin o creador propietario eliminan el acceso de un consumidor al dashboard.")
	public DashboardResponse unassignUsuario(@PathVariable UUID id, @PathVariable UUID usuarioId) {
		return dashboardService.unassignUsuario(id, usuarioId);
	}
}
