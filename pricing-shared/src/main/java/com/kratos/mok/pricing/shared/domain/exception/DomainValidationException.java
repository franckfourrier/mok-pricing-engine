package com.kratos.mok.pricing.shared.domain.exception;

import java.util.Map;

public class DomainValidationException extends RuntimeException {

    private final String code;
    private final Map<String, Object> details;

    public DomainValidationException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public String code() { return code; }

    public Map<String, Object> details() { return details; }
}
