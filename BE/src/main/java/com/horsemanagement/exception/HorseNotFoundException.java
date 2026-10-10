package com.horsemanagement.exception;

public class HorseNotFoundException extends RuntimeException {
    public HorseNotFoundException(Integer horseId) {
        super("Horse " + horseId + " was not found");
    }
}
