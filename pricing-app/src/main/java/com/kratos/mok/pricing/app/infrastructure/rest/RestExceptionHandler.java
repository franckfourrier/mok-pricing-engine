package com.kratos.mok.pricing.app.infrastructure.rest;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import com.kratos.mok.pricing.shared.domain.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {
    // (optionnel) si tu veux conditionner l’affichage en dev
    private final Environment env;

    public RestExceptionHandler(Environment env) {
        this.env = env;
    }
    // -----------------------------
    // 400 — BAD REQUEST
    // -----------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(
            MethodArgumentNotValidException ex
    ) {
        Map<String, Object> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fields.put(fe.getField(), fe.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(problem(
                "INVALID_REQUEST",
                "Request validation failed",
                fields
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity.badRequest().body(problem(
                "MALFORMED_JSON",
                "Malformed or unreadable JSON payload",
                Map.of()
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        return ResponseEntity.badRequest().body(problem(
                "CONSTRAINT_VIOLATION",
                ex.getMessage(),
                Map.of()
        ));
    }

    // -----------------------------
    // 401/403 — SECURITE
    // -----------------------------

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(401).body(problem(
                "UNAUTHORIZED",
                "Authentication required",
                Map.of()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(problem(
                "FORBIDDEN",
                "Access denied",
                Map.of()
        ));
    }
    // -----------------------------
    // 404 — NOT FOUND
    // -----------------------------
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem(
                ex.code(),
                ex.getMessage(),
                ex.details()
        ));
    }


    // -----------------------------
    // 422 — UNPROCESSABLE ENTITY
    // -----------------------------

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Map<String, Object>> handleDomainValidation(
            DomainValidationException ex
    ) {
        return ResponseEntity.status(422).body(problem(
                ex.code(),
                ex.getMessage(),
                ex.details()
        ));
    }

    // -----------------------------
    // 409 — CONFLIT
    // -----------------------------

    @ExceptionHandler(com.kratos.mok.pricing.shared.domain.exception.InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(
            com.kratos.mok.pricing.shared.domain.exception.InvalidStateException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem(
                ex.code(),
                ex.getMessage(),
                ex.details()
        ));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        // cas classique Enum.valueOf(...)
        if (ex.getMessage() != null && ex.getMessage().startsWith("No enum constant")) {
            return ResponseEntity.badRequest().body(problem(
                    "INVALID_ENUM",
                    ex.getMessage(),
                    Map.of()
            ));
        }
        // fallback métier (quand on n’a pas encore refactoré en DomainValidationException)
        return ResponseEntity.status(422).body(problem(
                "DOMAIN_RULE_VIOLATION",
                ex.getMessage(),
                Map.of()
        ));
    }

    // -----------------------------
    // 409 — CONFLICT (strict API)
    // -----------------------------

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(
            ConflictException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem(
                "CONFLICT",
                ex.getMessage(),
                Map.of()
        ));
    }

    // -----------------------------
    // 500 — INTERNAL SERVER ERROR
    // -----------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleFatal(Exception ex) {
        String traceId = java.util.UUID.randomUUID().toString();
        log.error("traceId={} Unhandled exception", traceId, ex);


        boolean isDev = java.util.Arrays.asList(env.getActiveProfiles()).contains("dev");

        return ResponseEntity.status(500).body(problem(
                "INTERNAL_ERROR",
                isDev ? ex.getClass().getSimpleName() + ": " + ex.getMessage() : "Unexpected technical error",
                Map.of("traceId", traceId)
        ));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<Map<String, Object>> handleJpaUsage(
            InvalidDataAccessApiUsageException ex
    ) {
        return ResponseEntity.status(500).body(problem(
                "DATA_ACCESS_ERROR",
                "Invalid query parameter type (internal configuration error)",
                Map.of()
        ));
    }


    // -----------------------------
    // Helper
    // -----------------------------

    private Map<String, Object> problem(
            String code,
            String message,
            Map<String, Object> details
    ) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "code", code,
                "message", message,
                "details", details == null ? Map.of() : details
        );
    }
}
