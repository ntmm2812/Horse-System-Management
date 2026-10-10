package com.horsemanagement.exception;

public class InjuryNotFoundException extends RuntimeException {
    public InjuryNotFoundException(Integer injuryId) {
        super("Injury " + injuryId + " not found");
    }
}
