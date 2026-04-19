package com.skillonnet.automation.service;

import com.skillonnet.automation.dao.AdverseReactionDAO;
import com.skillonnet.automation.dao.AppointmentDAO;
import com.skillonnet.automation.dao.IncidentDAO;
import com.skillonnet.automation.dao.MedicationDAO;
import com.skillonnet.automation.dao.PatientDAO;
import com.skillonnet.automation.dao.PrescriptionDAO;
import com.skillonnet.automation.dao.WarningOverrideDAO;
import com.skillonnet.automation.db.DBConnection;
import com.skillonnet.automation.exception.WarningException;
import com.skillonnet.automation.model.AdverseReaction;
import com.skillonnet.automation.model.Appointment;
import com.skillonnet.automation.model.Incident;
import com.skillonnet.automation.model.Medication;
import com.skillonnet.automation.model.Patient;
import com.skillonnet.automation.model.Prescription;
import com.skillonnet.automation.model.WarningOverride;
import com.skillonnet.automation.support.TestDbSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClinicalLogicServiceTest {

    private ClinicalLogicService service;
    private PatientDAO patientDAO;
    private MedicationDAO medicationDAO;
    private AppointmentDAO appointmentDAO;
    private AdverseReactionDAO adverseReactionDAO;
    private PrescriptionDAO prescriptionDAO;
    private WarningOverrideDAO warningOverrideDAO;

    @BeforeEach
    void setUp() {
        TestDbSupport.resetSchema();
        patientDAO = new PatientDAO();
        medicationDAO = new MedicationDAO();
        appointmentDAO = new AppointmentDAO();
        adverseReactionDAO = new AdverseReactionDAO();
        prescriptionDAO = new PrescriptionDAO();
        warningOverrideDAO = new WarningOverrideDAO();
        service = new ClinicalLogicService(
                adverseReactionDAO,
                prescriptionDAO,
                warningOverrideDAO,
                new IncidentDAO(),
                patientDAO);
    }

    @AfterEach
    void tearDown() {
        DBConnection.getInstance().closeConnection();
    }

    @Test
    void createPrescriptionThrowsWhenAdverseReactionExists() {
        int patientId = insertPatient();
        int medId = insertMedication();
        insertAdverseReaction(patientId, medId);
        int apptId = insertAppointment(patientId);
        Prescription rx = buildPrescription(apptId, medId, 99);

        WarningException ex = assertThrows(WarningException.class, () -> service.createPrescription(patientId, rx));
        assertEquals(patientId, ex.getPatientId());
        assertEquals(medId, ex.getMedicationId());
    }

    @Test
    void createPrescriptionWithOverrideInsertsOverrideAndPrescription() {
        int patientId = insertPatient();
        int medId = insertMedication();
        insertAdverseReaction(patientId, medId);
        int apptId = insertAppointment(patientId);
        int prescriberId = 42;
        Prescription rx = buildPrescription(apptId, medId, prescriberId);

        assertThrows(WarningException.class, () -> service.createPrescription(patientId, rx));

        WarningOverride ov = new WarningOverride();
        ov.setPrescriberId(prescriberId);
        ov.setMedicationId(medId);
        ov.setWarningDetails("acknowledged");
        ov.setOverrideDate(LocalDate.of(2026, 4, 1));

        int rxId = service.createPrescriptionWithOverride(patientId, rx, ov);
        assertTrue(rxId > 0);
        assertTrue(ov.getOverrideId() > 0);
        assertEquals(1L, warningOverrideDAO.countByPrescriberAndMedication(prescriberId, medId));
        assertEquals(1L, prescriptionDAO.countByPatientMedicationPrescriber(patientId, medId, prescriberId));
    }

    @Test
    void refreshSelfHarmHistoryFlagSetsTrueWhenDeliberateIncident() {
        int patientId = insertPatient();
        Incident inc = new Incident();
        inc.setPatientId(patientId);
        inc.setType("Deliberate");
        inc.setDescription("x");
        inc.setIncidentDate(LocalDate.of(2026, 1, 2));
        new IncidentDAO().insert(inc);

        service.refreshSelfHarmHistoryFlag(patientId);
        assertTrue(patientDAO.findById(patientId).orElseThrow().isSelfHarmHistory());
    }

    private int insertPatient() {
        Patient p = new Patient();
        p.setFirstName("P");
        p.setLastName("Q");
        p.setAddress(null);
        p.setHomeless(false);
        p.setRiskStatus(null);
        p.setDeceased(false);
        p.setSelfHarmHistory(false);
        return patientDAO.insert(p);
    }

    private int insertMedication() {
        Medication m = new Medication();
        m.setName("Med");
        return medicationDAO.insert(m);
    }

    private int insertAppointment(int patientId) {
        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setClinicId(1);
        a.setStaffId(1);
        a.setAppointmentDate(LocalDate.of(2026, 3, 1));
        a.setType("Visit");
        a.setStatus("Done");
        a.setRecordsUpdated(false);
        return appointmentDAO.insert(a);
    }

    private void insertAdverseReaction(int patientId, int medId) {
        AdverseReaction r = new AdverseReaction();
        r.setPatientId(patientId);
        r.setMedicationId(medId);
        r.setDescription("rash");
        adverseReactionDAO.insert(r);
    }

    private static Prescription buildPrescription(int apptId, int medId, int prescriberId) {
        Prescription rx = new Prescription();
        rx.setAppointmentId(apptId);
        rx.setMedicationId(medId);
        rx.setPrescriberId(prescriberId);
        rx.setIssueDate(LocalDate.of(2026, 4, 19));
        rx.setRepeat(false);
        return rx;
    }
}
