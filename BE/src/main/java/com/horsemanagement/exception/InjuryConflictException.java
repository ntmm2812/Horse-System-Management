package com.horsemanagement.exception;

public class InjuryConflictException extends RuntimeException {
    public InjuryConflictException(String message) {
        super(message);
    }

    public InjuryConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
