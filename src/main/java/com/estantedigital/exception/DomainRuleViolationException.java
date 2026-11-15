package com.estantedigital.exception;

public class DomainRuleViolationException extends RuntimeException {

    private final String field;

    public DomainRuleViolationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
