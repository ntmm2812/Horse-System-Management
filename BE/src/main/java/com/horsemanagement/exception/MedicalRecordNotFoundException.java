package com.horsemanagement.exception;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(Integer recordId) {
        super("Medical record " + recordId + " was not found");
    }
}
