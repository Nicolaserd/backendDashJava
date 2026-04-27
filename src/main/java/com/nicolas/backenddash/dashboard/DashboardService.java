package com.nicolas.backenddash.dashboard;

import com.nicolas.backenddash.dashboard.dto.DashboardRequest;
import com.nicolas.backenddash.dashboard.dto.DashboardResponse;
import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import com.nicolas.backenddash.security.AuthenticatedUser;
import com.nicolas.backenddash.security.AuthorizationService;
import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import com.nicolas.backenddash.usuario.UsuarioRol;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
public class DashboardService {

	private final DashboardRepository dashboardRepository;
	private final EmpresaRepository empresaRepository;
	private final UsuarioRepository usuarioRepository;
	private final AuthorizationService authorizationService;

	public DashboardService(
			DashboardRepository dashboardRepository,
			EmpresaRepository empresaRepository,
			UsuarioRepository usuarioRepository,
			AuthorizationService authorizationService
	) {
		this.dashboardRepository = dashboardRepository;
		this.empresaRepository = empresaRepository;
		this.usuarioRepository = usuarioRepository;
		this.authorizationService = authorizationService;
	}

	@Transactional(readOnly = true)
	public List<DashboardResponse> findAll() {
		AuthenticatedUser user = authorizationService.currentUser();
		UUID userEmpresaId = user.empresaId();
		List<Dashboard> dashboards;

		if (user.isAdmin()) {
			dashboards = userEmpresaId == null
					? dashboardRepository.findAll()
					: dashboardRepository.findByEmpresaId(userEmpresaId);
		} else if (user.isDashboardCreator()) {
			dashboards = userEmpresaId == null
					? dashboardRepository.findByCreadorId(user.id())
					: dashboardRepository.findByEmpresaIdAndCreadorId(userEmpresaId, user.id());
		} else if (user.isDashboardConsumer()) {
			dashboards = userEmpresaId == null
					? dashboardRepository.findByUsuariosAsignados_Id(user.id())
					: dashboardRepository.findByEmpresaIdAndUsuariosAsignados_Id(userEmpresaId, user.id());
		} else {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard access denied");
		}

		return dashboards
				.stream()
				.map(DashboardResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DashboardResponse findById(UUID id) {
		Dashboard dashboard = findEntity(id);
		requireSameEmpresaScope(dashboard);
		requireCanView(dashboard);
		return DashboardResponse.from(dashboard);
	}

	public DashboardResponse create(DashboardRequest request) {
		authorizationService.requireAdminOrDashboardCreator();
		Empresa empresa = resolveDashboardEmpresa(request.empresaId());
		Usuario creador = resolveCreator(request.creadorId(), empresa);
		Dashboard dashboard = new Dashboard(request.nombre(), request.tipo(), request.contenido(), creador, empresa);
		return DashboardResponse.from(dashboardRepository.save(dashboard));
	}

	public DashboardResponse update(UUID id, DashboardRequest request) {
		Dashboard dashboard = findEntity(id);
		requireSameEmpresaScope(dashboard);
		requireCanManage(dashboard);
		Empresa targetEmpresa = resolveDashboardEmpresaForUpdate(dashboard, request.empresaId());
		boolean empresaChanged = !sameEmpresa(dashboard.getEmpresa(), targetEmpresa);
		if (empresaChanged && !dashboard.getUsuariosAsignados().isEmpty()) {
			throw new ResponseStatusException(CONFLICT, "Unassign consumers before changing dashboard empresa");
		}
		if (empresaChanged
				&& request.creadorId() == null
				&& dashboard.getCreador() != null
				&& !sameEmpresa(dashboard.getCreador().getEmpresa(), targetEmpresa)) {
			throw new ResponseStatusException(FORBIDDEN, "creadorId is required when changing dashboard empresa");
		}
		dashboard.setNombre(request.nombre());
		dashboard.setTipo(request.tipo());
		dashboard.setContenido(request.contenido());
		dashboard.setEmpresa(targetEmpresa);
		if (authorizationService.currentUser().isAdmin() && request.creadorId() != null) {
			dashboard.setCreador(findCreatorForEmpresa(request.creadorId(), targetEmpresa));
		}
		return DashboardResponse.from(dashboard);
	}

	public void delete(UUID id) {
		Dashboard dashboard = findEntity(id);
		requireSameEmpresaScope(dashboard);
		requireCanManage(dashboard);
		dashboardRepository.delete(dashboard);
	}

	public DashboardResponse assignUsuario(UUID dashboardId, UUID usuarioId) {
		Dashboard dashboard = findEntity(dashboardId);
		requireSameEmpresaScope(dashboard);
		requireCanManage(dashboard);
		Usuario usuario = findUsuario(usuarioId);
		if (usuario.getRol() != UsuarioRol.DASHBOARD_USUARIO) {
			throw new ResponseStatusException(FORBIDDEN, "Only dashboard consumers can be assigned");
		}
		if (!sameEmpresa(dashboard.getEmpresa(), usuario.getEmpresa())) {
			throw new ResponseStatusException(FORBIDDEN, "Usuario must belong to the same empresa as the dashboard");
		}
		boolean alreadyAssigned = dashboard.getUsuariosAsignados()
				.stream()
				.anyMatch(assigned -> assigned.getId().equals(usuarioId));
		if (alreadyAssigned) {
			throw new ResponseStatusException(CONFLICT, "Usuario is already assigned to this dashboard");
		}
		dashboard.assignUsuario(usuario);
		return DashboardResponse.from(dashboard);
	}

	public DashboardResponse unassignUsuario(UUID dashboardId, UUID usuarioId) {
		Dashboard dashboard = findEntity(dashboardId);
		requireSameEmpresaScope(dashboard);
		requireCanManage(dashboard);
		Usuario usuario = findUsuario(usuarioId);
		if (usuario.getRol() != UsuarioRol.DASHBOARD_USUARIO) {
			throw new ResponseStatusException(FORBIDDEN, "Only dashboard consumers can be unassigned");
		}
		dashboard.unassignUsuario(usuario);
		return DashboardResponse.from(dashboard);
	}

	private Dashboard findEntity(UUID id) {
		return dashboardRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Dashboard not found"));
	}

	private Usuario findUsuario(UUID id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuario not found"));
	}

	private Usuario resolveCreator(UUID requestedCreatorId, Empresa empresa) {
		AuthenticatedUser user = authorizationService.currentUser();
		if (user.isAdmin() && requestedCreatorId != null) {
			return findCreatorForEmpresa(requestedCreatorId, empresa);
		}
		Usuario currentUser = findUsuario(user.id());
		if (!sameEmpresa(currentUser.getEmpresa(), empresa)) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard creator must belong to the dashboard empresa");
		}
		return currentUser;
	}

	private void requireCanView(Dashboard dashboard) {
		AuthenticatedUser user = authorizationService.currentUser();
		if (user.isAdmin() || isCreatorOwner(dashboard, user.id()) || isAssignedConsumer(dashboard, user.id())) {
			return;
		}
		throw new ResponseStatusException(FORBIDDEN, "Dashboard access denied");
	}

	private void requireCanManage(Dashboard dashboard) {
		AuthenticatedUser user = authorizationService.currentUser();
		if (user.isAdmin() || (user.isDashboardCreator() && isCreatorOwner(dashboard, user.id()))) {
			return;
		}
		throw new ResponseStatusException(FORBIDDEN, "Dashboard management denied");
	}

	private boolean isCreatorOwner(Dashboard dashboard, UUID usuarioId) {
		return dashboard.getCreador() != null && dashboard.getCreador().getId().equals(usuarioId);
	}

	private boolean isAssignedConsumer(Dashboard dashboard, UUID usuarioId) {
		return dashboard.getUsuariosAsignados()
				.stream()
				.anyMatch(usuario -> usuario.getId().equals(usuarioId));
	}

	private Empresa resolveDashboardEmpresa(UUID requestedEmpresaId) {
		AuthenticatedUser user = authorizationService.currentUser();
		if (user.isSuperAdmin()) {
			if (requestedEmpresaId == null) {
				throw new ResponseStatusException(FORBIDDEN, "empresaId is required for super admin dashboard creation");
			}
			return findEmpresa(requestedEmpresaId);
		}
		if (user.isAdmin()) {
			if (user.empresaId() != null) {
				if (requestedEmpresaId != null && !requestedEmpresaId.equals(user.empresaId())) {
					throw new ResponseStatusException(FORBIDDEN, "Admin can only manage dashboards from their empresa");
				}
				return findEmpresa(user.empresaId());
			}
			if (requestedEmpresaId == null) {
				throw new ResponseStatusException(FORBIDDEN, "empresaId is required for global admin dashboard creation");
			}
			return findEmpresa(requestedEmpresaId);
		}

		Usuario creator = findUsuario(user.id());
		if (creator.getEmpresa() == null) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard creator has no empresa assigned");
		}
		if (requestedEmpresaId != null && !requestedEmpresaId.equals(creator.getEmpresa().getId())) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard creator can only create dashboards in their empresa");
		}
		return creator.getEmpresa();
	}

	private Empresa findEmpresa(UUID empresaId) {
		return empresaRepository.findById(empresaId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Empresa not found"));
	}

	private Empresa resolveDashboardEmpresaForUpdate(Dashboard dashboard, UUID requestedEmpresaId) {
		Empresa currentEmpresa = dashboard.getEmpresa();
		UUID currentEmpresaId = currentEmpresa != null ? currentEmpresa.getId() : null;
		if (requestedEmpresaId == null || requestedEmpresaId.equals(currentEmpresaId)) {
			return currentEmpresa;
		}
		AuthenticatedUser user = authorizationService.currentUser();
		if (!user.isSuperAdmin()) {
			throw new ResponseStatusException(FORBIDDEN, "Only SUPER_ADMIN can change dashboard empresa");
		}
		return findEmpresa(requestedEmpresaId);
	}

	private Usuario findCreatorForEmpresa(UUID creatorId, Empresa empresa) {
		Usuario creator = findUsuario(creatorId);
		if (creator.getRol() != UsuarioRol.DASHBOARD_CREADOR) {
			throw new ResponseStatusException(FORBIDDEN, "Creator must have DASHBOARD_CREADOR role");
		}
		if (!sameEmpresa(creator.getEmpresa(), empresa)) {
			throw new ResponseStatusException(FORBIDDEN, "Creator must belong to the dashboard empresa");
		}
		return creator;
	}

	private void requireSameEmpresaScope(Dashboard dashboard) {
		AuthenticatedUser user = authorizationService.currentUser();
		if (!user.isAdmin() || user.empresaId() == null) {
			return;
		}
		if (dashboard.getEmpresa() == null || !dashboard.getEmpresa().getId().equals(user.empresaId())) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard does not belong to your empresa");
		}
	}

	private boolean sameEmpresa(Empresa left, Empresa right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null || right == null) {
			return false;
		}
		return left.getId().equals(right.getId());
	}
}
