package com.horsemanagement.exception;

public class TreatmentPlanNotFoundException extends RuntimeException {
    public TreatmentPlanNotFoundException(Integer treatmentId) {
        super("Treatment plan " + treatmentId + " was not found");
    }
}
