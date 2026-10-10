package com.horsemanagement.exception;

public class VeterinarianNotFoundException extends RuntimeException {
    public VeterinarianNotFoundException(Integer vetId) {
        super("Veterinarian user " + vetId + " was not found");
    }
}
