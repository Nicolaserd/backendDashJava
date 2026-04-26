package com.nicolas.backenddash.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public String hash(String password) {
		return passwordEncoder.encode(password);
	}

	public boolean matches(String password, String passwordHash) {
		return passwordEncoder.matches(password, passwordHash);
	}
}
