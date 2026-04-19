package com.skillonnet.automation.exception;

public class WarningException extends Exception {

    private final int patientId;
    private final int medicationId;

    public WarningException(int patientId, int medicationId) {
        super("Patient has recorded adverse reaction for this medication");
        this.patientId = patientId;
        this.medicationId = medicationId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getMedicationId() {
        return medicationId;
    }
}
