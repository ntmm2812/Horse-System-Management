package com.horsemanagement.exception;

public class SupplyNotFoundException extends RuntimeException {
    public SupplyNotFoundException(Integer supplyId) {
        super("Supply " + supplyId + " was not found");
    }
}
