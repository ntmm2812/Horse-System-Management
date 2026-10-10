package com.horsemanagement.exception;

public class IncidentAlreadyLinkedException extends RuntimeException {
    public IncidentAlreadyLinkedException(Integer incidentId) {
        super("Incident " + incidentId + " is already linked to a medical record");
    }
}
