-- Smoke-test seed for Mental Health API (MySQL 8+).
-- Apply after schema.sql on database `mental_health`:
--   mysql -u USER -p mental_health < src/main/resources/schema.sql
--   mysql -u USER -p mental_health < scripts/seed-smoke.sql
--
-- Resets application data and loads a deterministic story:
--   clinical1 / reception1 / records1 (password: secret)
--   One Missed appointment on 2026-01-15 + prescription for reporting.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE prescription;
TRUNCATE TABLE incident;
TRUNCATE TABLE adverse_reaction;
TRUNCATE TABLE warning_override;
TRUNCATE TABLE appointment;
TRUNCATE TABLE change_requests;
TRUNCATE TABLE patients;
TRUNCATE TABLE medication;
TRUNCATE TABLE clinics;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (username, password_hash, role) VALUES
  ('clinical1', 'secret', 'Clinical'),
  ('reception1', 'secret', 'Receptionist'),
  ('records1', 'secret', 'Medical_Records');

INSERT INTO clinics (name, location_type) VALUES ('Demo Clinic', 'Hospital');

INSERT INTO patients (first_name, last_name, address, homeless, deceased, self_harm_history)
VALUES ('Jane', 'Doe', '1 Test St', 0, 0, 0);

INSERT INTO medication (name) VALUES ('Aspirin');

-- staff_id = 1 (clinical1); Missed + records not updated → missed list + pending-records
INSERT INTO appointment (patient_id, clinic_id, staff_id, appointment_date, type, status, records_updated)
VALUES (1, 1, 1, '2026-01-15', 'Drop-in', 'Missed', 0);

INSERT INTO prescription (appointment_id, medication_id, prescriber_id, issue_date, repeat_presc)
VALUES (1, 1, 1, '2026-01-15', 0);
