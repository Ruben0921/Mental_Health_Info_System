package com.skillonnet.automation.exception;

public class DeceasedPatientException extends RuntimeException {

    public DeceasedPatientException() {
        super("Patient record is read-only (deceased)");
    }
}
