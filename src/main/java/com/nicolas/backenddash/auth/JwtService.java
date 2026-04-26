package com.nicolas.backenddash.auth;

import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioRol;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class JwtService {

	private final String secret;
	private final long expirationSeconds;

	public JwtService(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-seconds}") long expirationSeconds
	) {
		this.secret = secret;
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(Usuario usuario) {
		Instant now = Instant.now();
		String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payload = "{"
				+ "\"sub\":\"" + escape(usuario.getEmail()) + "\","
				+ "\"usuarioId\":\"" + usuario.getId() + "\","
				+ "\"empresaId\":" + (usuario.getEmpresa() != null ? "\"" + usuario.getEmpresa().getId() + "\"" : "null") + ","
				+ "\"rol\":\"" + usuario.getRol().name() + "\","
				+ "\"nombre\":\"" + escape(usuario.getNombre()) + "\","
				+ "\"iat\":" + now.getEpochSecond() + ","
				+ "\"exp\":" + now.plusSeconds(expirationSeconds).getEpochSecond()
				+ "}";

		String unsignedToken = base64Url(header.getBytes(StandardCharsets.UTF_8))
				+ "."
				+ base64Url(payload.getBytes(StandardCharsets.UTF_8));
		return unsignedToken + "." + sign(unsignedToken);
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	public JwtClaims validateToken(String token) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
		}

		String unsignedToken = parts[0] + "." + parts[1];
		if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
		}

		String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		long expiration = extractLong(payload, "exp");
		if (Instant.now().getEpochSecond() >= expiration) {
			throw new ResponseStatusException(UNAUTHORIZED, "Token expired");
		}

		return new JwtClaims(
				UUID.fromString(extractString(payload, "usuarioId")),
				extractString(payload, "sub"),
				UsuarioRol.valueOf(extractString(payload, "rol")),
				extractNullableUuid(payload, "empresaId")
		);
	}

	private String sign(String unsignedToken) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to sign JWT", exception);
		}
	}

	private String base64Url(byte[] value) {
		return Base64.getUrlEncoder()
				.withoutPadding()
				.encodeToString(value);
	}

	private String escape(String value) {
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"");
	}

	private String extractString(String payload, String field) {
		String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
		java.util.regex.Matcher matcher = Pattern.compile(pattern).matcher(payload);
		if (!matcher.find()) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
		}
		return matcher.group(1);
	}

	private long extractLong(String payload, String field) {
		String pattern = "\"" + field + "\"\\s*:\\s*(\\d+)";
		java.util.regex.Matcher matcher = Pattern.compile(pattern).matcher(payload);
		if (!matcher.find()) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
		}
		return Long.parseLong(matcher.group(1));
	}

	private UUID extractNullableUuid(String payload, String field) {
		String stringPattern = "\"" + field + "\"\\s*:\\s*\"([^\"]+)\"";
		java.util.regex.Matcher stringMatcher = Pattern.compile(stringPattern).matcher(payload);
		if (stringMatcher.find()) {
			return UUID.fromString(stringMatcher.group(1));
		}

		String nullPattern = "\"" + field + "\"\\s*:\\s*null";
		java.util.regex.Matcher nullMatcher = Pattern.compile(nullPattern).matcher(payload);
		if (nullMatcher.find()) {
			return null;
		}
		return null;
	}

	private boolean constantTimeEquals(String expected, String actual) {
		return java.security.MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				actual.getBytes(StandardCharsets.UTF_8)
		);
	}
}
