package com.horsemanagement.exception;

public class PreventiveCareConflictException extends RuntimeException {
    public PreventiveCareConflictException(String message) { super(message); }
    public PreventiveCareConflictException(String message, Throwable cause) { super(message, cause); }
}
