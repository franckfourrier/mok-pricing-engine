package com.kratos.mok.pricing.app.infrastructure.rest.exception;

import com.kratos.mok.pricing.shared.domain.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    private final Environment env;

    public RestExceptionHandler(Environment env) {
        this.env = env;
    }

    // ======================================================
    // TRACE ID
    // ======================================================

    private String traceId() {
        String id = UUID.randomUUID().toString();
        MDC.put("traceId", id);
        return id;
    }

    private void clearMdc() {
        MDC.clear();
    }

    // ======================================================
    // RESPONSE BUILDER
    // ======================================================

    private ResponseEntity<Map<String, Object>> response(
            HttpStatus status,
            String code,
            String message,
            Map<String, Object> details,
            String traceId
    ) {
        try {
            Map<String, Object> body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "traceId", traceId,
                    "code", code,
                    "message", message == null ? "" : message,
                    "details", details == null ? Map.of() : details
            );

            return ResponseEntity.status(status).body(body);
        } finally {
            clearMdc();
        }
    }

    // ======================================================
    // 400 - VALIDATION
    // ======================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        String traceId = traceId();

        Map<String, Object> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("[VALIDATION] traceId={} fields={}", traceId, fields);

        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request validation failed",
                fields,
                traceId
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJson(HttpMessageNotReadableException ex) {

        String traceId = traceId();

        log.warn("[MALFORMED_JSON] traceId={}", traceId);

        return response(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_JSON",
                "Malformed JSON request",
                Map.of(),
                traceId
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                ex.getMessage(),
                Map.of(),
                traceId
        );
    }

    // ======================================================
    // 401 / 403
    // ======================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required",
                Map.of(),
                traceId
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Access denied",
                Map.of(),
                traceId
        );
    }

    // ======================================================
    // 404
    // ======================================================

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.NOT_FOUND,
                ex.code(),
                ex.getMessage(),
                ex.details(),
                traceId
        );
    }

    // ======================================================
    // 409
    // ======================================================

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.CONFLICT,
                "CONFLICT",
                ex.getMessage(),
                Map.of(),
                traceId
        );
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handleState(InvalidStateException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.CONFLICT,
                ex.code(),
                ex.getMessage(),
                ex.details(),
                traceId
        );
    }

    // ======================================================
    // 422 DOMAIN
    // ======================================================

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Map<String, Object>> handleDomain(DomainValidationException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.code(),
                ex.getMessage(),
                ex.details(),
                traceId
        );
    }

    // ======================================================
    // ENUM + ARGUMENT ERROR
    // ======================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {

        String traceId = traceId();

        String code = (ex.getMessage() != null && ex.getMessage().startsWith("No enum constant"))
                ? "INVALID_ENUM"
                : "DOMAIN_RULE_VIOLATION";

        HttpStatus status = code.equals("INVALID_ENUM")
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.UNPROCESSABLE_ENTITY;

        return response(
                status,
                code,
                ex.getMessage(),
                Map.of(),
                traceId
        );
    }

    // ======================================================
    // SPRING RESPONSE STATUS EXCEPTION
    // ======================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {

        String traceId = traceId();

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        String code = switch (status) {
            case BAD_REQUEST -> "INVALID_ARGUMENT";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            default -> "HTTP_ERROR";
        };

        log.warn("[HTTP_EXCEPTION] traceId={} status={} reason={}",
                traceId, status, ex.getReason());

        return response(
                status,
                code,
                ex.getReason(),
                Map.of(),
                traceId
        );
    }

    // ======================================================
    // INFRA
    // ======================================================

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<Map<String, Object>> handleJpa(InvalidDataAccessApiUsageException ex) {

        String traceId = traceId();

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DATA_ACCESS_ERROR",
                "Invalid query parameter type",
                Map.of(),
                traceId
        );
    }

    // ======================================================
    // FALLBACK
    // ======================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleFatal(Exception ex) {

        String traceId = traceId();

        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        log.error("[FATAL] traceId={}", traceId, ex);

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                isDev ? ex.getMessage() : "Unexpected technical error",
                Map.of(),
                traceId
        );
    }
}