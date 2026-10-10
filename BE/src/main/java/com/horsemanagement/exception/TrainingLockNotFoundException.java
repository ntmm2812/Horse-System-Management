package com.horsemanagement.exception;

public class TrainingLockNotFoundException extends RuntimeException {
    public TrainingLockNotFoundException(Integer lockId) {
        super("Training lock " + lockId + " not found");
    }
}
