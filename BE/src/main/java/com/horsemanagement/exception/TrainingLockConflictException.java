package com.horsemanagement.exception;

public class TrainingLockConflictException extends RuntimeException {
    public TrainingLockConflictException(String message) {
        super(message);
    }

    public TrainingLockConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
