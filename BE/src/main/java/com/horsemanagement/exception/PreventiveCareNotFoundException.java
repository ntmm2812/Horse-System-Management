package com.horsemanagement.exception;

public class PreventiveCareNotFoundException extends RuntimeException {
    public PreventiveCareNotFoundException(Integer id) {
        super("Preventive care schedule " + id + " not found");
    }
}
