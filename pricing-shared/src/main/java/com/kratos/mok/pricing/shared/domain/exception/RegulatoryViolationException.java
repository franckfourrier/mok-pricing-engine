package com.kratos.mok.pricing.shared.domain.exception;

public class RegulatoryViolationException extends RuntimeException {

    private final String regulationCode;

    public RegulatoryViolationException(String message) {
        super(message);
        this.regulationCode = "UNKNOWN_REGULATION";
    }

    public RegulatoryViolationException(String regulationCode, String message) {
        super(message);
        this.regulationCode = regulationCode;
    }

    public String regulationCode() {
        return regulationCode;
    }
}
