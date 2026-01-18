package com.kratos.mok.pricing.app.infrastructure.rest.fees;

import com.kratos.mok.pricing.shared.domain.exception.DomainValidationException;
import com.kratos.mok.pricing.shared.domain.exception.ConflictException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
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
        return ResponseEntity.status(500).body(problem(
                "INTERNAL_ERROR",
                "Unexpected technical error",
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
