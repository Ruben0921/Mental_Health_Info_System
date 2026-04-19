package com.skillonnet.automation.model;

public class AdverseReaction {

    private int reactionId;
    private int patientId;
    private int medicationId;
    private String description;

    public int getReactionId() {
        return reactionId;
    }

    public void setReactionId(int reactionId) {
        this.reactionId = reactionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(int medicationId) {
        this.medicationId = medicationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
