package com.nicolas.backenddash.security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class AuthContext {

	private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

	public void setCurrentUser(AuthenticatedUser user) {
		CURRENT_USER.set(user);
	}

	public AuthenticatedUser getCurrentUser() {
		AuthenticatedUser user = CURRENT_USER.get();
		if (user == null) {
			throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
		}
		return user;
	}

	public void clear() {
		CURRENT_USER.remove();
	}
}
