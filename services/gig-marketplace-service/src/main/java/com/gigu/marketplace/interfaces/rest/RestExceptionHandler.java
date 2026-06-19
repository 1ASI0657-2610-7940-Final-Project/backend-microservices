package com.gigu.marketplace.interfaces.rest;

import com.gigu.marketplace.application.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    record ErrorResponse(Instant timestamp, int status, String error, String message, String path, Map<String, String> fields) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fields = e.getBindingResult().getFieldErrors().stream().collect(java.util.stream.Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request contains invalid fields.", request, fields);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException e, HttpServletRequest request) { return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage(), request, Map.of()); }

    @ExceptionHandler(DuplicatedResourceException.class)
    ResponseEntity<ErrorResponse> duplicate(DuplicatedResourceException e, HttpServletRequest request) {
        String code = e.getMessage()!=null && e.getMessage().toLowerCase().contains("email") ? "EMAIL_ALREADY_EXISTS" : "DUPLICATED_REVIEW";
        return build(HttpStatus.CONFLICT, code, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    ResponseEntity<ErrorResponse> invalidTransition(InvalidStateTransitionException e, HttpServletRequest request) { return build(HttpStatus.CONFLICT, "INVALID_PROJECT_STATUS_TRANSITION", e.getMessage(), request, Map.of()); }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    ResponseEntity<ErrorResponse> external(ExternalServiceUnavailableException e, HttpServletRequest request) { return build(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_UNAVAILABLE", e.getMessage(), request, Map.of()); }

    @ExceptionHandler(SupabaseStorageException.class)
    ResponseEntity<ErrorResponse> supabaseStorage(SupabaseStorageException e, HttpServletRequest request) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "SUPABASE_STORAGE_UNAVAILABLE", e.getMessage(), request, Map.of());
    }

    @ExceptionHandler({UnauthorizedActionException.class, SecurityException.class, AccessDeniedException.class})
    ResponseEntity<ErrorResponse> forbidden(Exception e, HttpServletRequest request) { return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Forbidden", request, Map.of()); }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    ResponseEntity<ErrorResponse> unauthorized(AuthenticationCredentialsNotFoundException e, HttpServletRequest request) { return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized", request, Map.of()); }

    @ExceptionHandler({BusinessRuleViolationException.class, IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> badRequest(RuntimeException e, HttpServletRequest request) { return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage(), request, Map.of()); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> internal(Exception e, HttpServletRequest request) {
        log.error("Unhandled error at {}", request.getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected internal error.", request, Map.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, HttpServletRequest request, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), error, message, request.getRequestURI(), fields));
    }
}
