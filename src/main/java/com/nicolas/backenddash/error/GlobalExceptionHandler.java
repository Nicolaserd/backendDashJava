package com.nicolas.backenddash.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		List<ApiFieldError> details = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", details, request);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
			ConstraintViolationException exception,
			HttpServletRequest request
	) {
		List<ApiFieldError> details = exception.getConstraintViolations()
				.stream()
				.map(violation -> new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()))
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", details, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleNotReadable(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		String message = "Invalid request body";
		List<ApiFieldError> details = new ArrayList<>();
		Throwable cause = exception.getCause();

		if (cause instanceof InvalidFormatException invalidFormatException) {
			String field = invalidFormatException.getPath().isEmpty()
					? "body"
					: invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getFieldName();
			String targetType = invalidFormatException.getTargetType() != null
					? invalidFormatException.getTargetType().getSimpleName()
					: "expected type";
			details.add(new ApiFieldError(field, "Invalid value for type " + targetType));
		} else {
			details.add(new ApiFieldError("body", "Malformed JSON or missing required fields"));
		}

		return build(HttpStatus.BAD_REQUEST, message, details, request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
			MethodArgumentTypeMismatchException exception,
			HttpServletRequest request
	) {
		List<ApiFieldError> details = List.of(
				new ApiFieldError(exception.getName(), "Invalid format")
		);
		return build(HttpStatus.BAD_REQUEST, "Invalid path/query parameter", details, request);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingParam(
			MissingServletRequestParameterException exception,
			HttpServletRequest request
	) {
		List<ApiFieldError> details = List.of(
				new ApiFieldError(exception.getParameterName(), "Required parameter is missing")
		);
		return build(HttpStatus.BAD_REQUEST, "Missing request parameter", details, request);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatus(
			ResponseStatusException exception,
			HttpServletRequest request
	) {
		HttpStatusCode statusCode = exception.getStatusCode();
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		String message = exception.getReason() != null ? exception.getReason() : "Request failed";
		return build(status != null ? status : HttpStatus.BAD_REQUEST, message, List.of(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
			DataIntegrityViolationException exception,
			HttpServletRequest request
	) {
		LOGGER.error("Data integrity error at {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
		return build(HttpStatus.CONFLICT, "Resource conflict", List.of(), request);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiErrorResponse> handleDataAccess(
			DataAccessException exception,
			HttpServletRequest request
	) {
		LOGGER.error("Database access error at {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed", List.of(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
			Exception exception,
			HttpServletRequest request
	) {
		LOGGER.error("Unhandled error at {} {}", request.getMethod(), request.getRequestURI(), exception);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", List.of(), request);
	}

	private ResponseEntity<ApiErrorResponse> build(
			HttpStatus status,
			String message,
			List<ApiFieldError> details,
			HttpServletRequest request
	) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				details
		);
		return ResponseEntity.status(status).body(body);
	}
}
