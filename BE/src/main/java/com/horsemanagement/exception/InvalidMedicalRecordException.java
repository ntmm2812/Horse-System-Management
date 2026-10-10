package com.horsemanagement.exception;

public class InvalidMedicalRecordException extends RuntimeException {
    public InvalidMedicalRecordException(String message) {
        super(message);
    }
}
