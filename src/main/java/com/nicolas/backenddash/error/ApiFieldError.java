package com.nicolas.backenddash.error;

public record ApiFieldError(
		String field,
		String message
) {
}
