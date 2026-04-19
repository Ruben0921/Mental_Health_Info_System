package com.skillonnet.automation.service;

import com.skillonnet.automation.dao.AdverseReactionDAO;
import com.skillonnet.automation.dao.IncidentDAO;
import com.skillonnet.automation.dao.PatientDAO;
import com.skillonnet.automation.dao.PrescriptionDAO;
import com.skillonnet.automation.dao.WarningOverrideDAO;
import com.skillonnet.automation.exception.WarningException;
import com.skillonnet.automation.model.Prescription;
import com.skillonnet.automation.model.WarningOverride;

public class ClinicalLogicService {

    private final AdverseReactionDAO adverseReactionDAO;
    private final PrescriptionDAO prescriptionDAO;
    private final WarningOverrideDAO warningOverrideDAO;
    private final IncidentDAO incidentDAO;
    private final PatientDAO patientDAO;

    public ClinicalLogicService() {
        this(
                new AdverseReactionDAO(),
                new PrescriptionDAO(),
                new WarningOverrideDAO(),
                new IncidentDAO(),
                new PatientDAO());
    }

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

    public int createPrescription(int patientId, Prescription prescription) throws WarningException {
        if (adverseReactionDAO.existsByPatientAndMedication(patientId, prescription.getMedicationId())) {
            throw new WarningException(patientId, prescription.getMedicationId());
        }
        return prescriptionDAO.insert(prescription);
    }

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

    public void refreshSelfHarmHistoryFlag(int patientId) {
        if (incidentDAO.hasDeliberateSelfHarm(patientId)) {
            patientDAO.updateSelfHarmHistory(patientId, true);
        }
    }
}
