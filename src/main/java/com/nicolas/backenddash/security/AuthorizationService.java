package com.nicolas.backenddash.security;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
public class AuthorizationService {

	private final AuthContext authContext;

	public AuthorizationService(AuthContext authContext) {
		this.authContext = authContext;
	}

	public AuthenticatedUser currentUser() {
		return authContext.getCurrentUser();
	}

	public void requireAdmin() {
		if (!currentUser().isAdmin()) {
			throw new ResponseStatusException(FORBIDDEN, "Admin role required");
		}
	}

	public void requireAdminOrDashboardCreator() {
		AuthenticatedUser user = currentUser();
		if (!user.isAdmin() && !user.isDashboardCreator()) {
			throw new ResponseStatusException(FORBIDDEN, "Dashboard creator role required");
		}
	}

	public UUID currentEmpresaId() {
		AuthenticatedUser user = currentUser();
		if (user.isSuperAdmin()) {
			return null;
		}
		return user.empresaId();
	}

	public boolean isGlobalAdmin() {
		AuthenticatedUser user = currentUser();
		return user.isSuperAdmin() || (user.isAdmin() && user.empresaId() == null);
	}

	public void requireGlobalAdmin() {
		if (!isGlobalAdmin()) {
			throw new ResponseStatusException(FORBIDDEN, "Global admin role required");
		}
	}
}
