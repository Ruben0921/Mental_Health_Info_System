package com.skillonnet.automation.service;

import com.skillonnet.automation.dao.AdverseReactionDAO;
import com.skillonnet.automation.dao.IncidentDAO;
import com.skillonnet.automation.dao.PatientDAO;
import com.skillonnet.automation.dao.PrescriptionDAO;
import com.skillonnet.automation.dao.WarningOverrideDAO;
import com.skillonnet.automation.exception.WarningException;
import com.skillonnet.automation.model.Prescription;
import com.skillonnet.automation.model.WarningOverride;

/**
 * Domain rules for prescriptions, overrides, and self-harm flags (used by tests and future APIs).
 */
public class ClinicalLogicService {

    private final AdverseReactionDAO adverseReactionDAO;
    private final PrescriptionDAO prescriptionDAO;
    private final WarningOverrideDAO warningOverrideDAO;
    private final IncidentDAO incidentDAO;
    private final PatientDAO patientDAO;

    /** Wires default DAO implementations. */
    public ClinicalLogicService() {
        this(
                new AdverseReactionDAO(),
                new PrescriptionDAO(),
                new WarningOverrideDAO(),
                new IncidentDAO(),
                new PatientDAO());
    }

    /**
     * @param adverseReactionDAO adverse reaction lookup
     * @param prescriptionDAO prescription persistence
     * @param warningOverrideDAO override persistence
     * @param incidentDAO incident queries
     * @param patientDAO patient updates
     */
    public ClinicalLogicService(
            AdverseReactionDAO adverseReactionDAO,
            PrescriptionDAO prescriptionDAO,
            WarningOverrideDAO warningOverrideDAO,
            IncidentDAO incidentDAO,
            PatientDAO patientDAO) {
        this.adverseReactionDAO = adverseReactionDAO;
        this.prescriptionDAO = prescriptionDAO;
        this.warningOverrideDAO = warningOverrideDAO;
        this.incidentDAO = incidentDAO;
        this.patientDAO = patientDAO;
    }

    /**
     * Inserts a prescription unless an adverse reaction blocks the medication for this patient.
     *
     * @throws WarningException when prescription should be blocked pending override
     */
    public int createPrescription(int patientId, Prescription prescription) throws WarningException {
        if (adverseReactionDAO.existsByPatientAndMedication(patientId, prescription.getMedicationId())) {
            throw new WarningException(patientId, prescription.getMedicationId());
        }
        return prescriptionDAO.insert(prescription);
    }

    /**
     * Records a prescriber override then inserts the prescription.
     *
     * @throws IllegalArgumentException when override does not match prescription
     * @throws IllegalStateException when no adverse reaction exists to override
     */
    public int createPrescriptionWithOverride(int patientId, Prescription prescription, WarningOverride override) {
        if (override.getPrescriberId() != prescription.getPrescriberId()) {
            throw new IllegalArgumentException("Override prescriber must match prescription");
        }
        if (override.getMedicationId() != prescription.getMedicationId()) {
            throw new IllegalArgumentException("Override medication must match prescription");
        }
        if (!adverseReactionDAO.existsByPatientAndMedication(patientId, prescription.getMedicationId())) {
            throw new IllegalStateException("No adverse reaction on record for override");
        }
        warningOverrideDAO.insert(override);
        return prescriptionDAO.insert(prescription);
    }

    /** Sets {@code self_harm_history} if deliberate self-harm incidents exist for the patient. */
    public void refreshSelfHarmHistoryFlag(int patientId) {
        if (incidentDAO.hasDeliberateSelfHarm(patientId)) {
            patientDAO.updateSelfHarmHistory(patientId, true);
        }
    }
}
