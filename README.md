# Mental Health Information System

Java21 / Maven backend for mental health clinic workflows: JDBC data access, role-based REST APIs (Jersey + Grizzly), and JSON via Jackson. The domain covers patients, appointments, prescriptions, incidents, adverse reactions, and change requests for medical records.

## Requirements

- JDK 21  
- Maven 3.9+  
- MySQL 8 (for running the HTTP API against a real database)

## Quick start

```bash
mvn test
```

Tests use an in-memory H2 database (`src/test/resources`) and do not require MySQL.

### Run the REST API

1. Create a MySQL database and apply [`src/main/resources/schema.sql`](src/main/resources/schema.sql).  
2. Insert at least one row into `users` (and seed `clinics` if you create appointments with foreign keys). For local demos, `UserDAO` compares passwords to `password_hash` as plain text.  
3. Set [`src/main/resources/config.properties`](src/main/resources/config.properties) (`db.url`, `db.username`, `db.password`, optional `api.baseUri`).  
4. Start the server by running [`com.skillonnet.automation.Main`](src/main/java/com/skillonnet/automation/Main.java) from your IDE, or after `mvn package` with `java` on the classpath (Jersey + Grizzly + your JAR).

All API routes expect **HTTP Basic Auth**. Role names must be exactly: `Clinical`, `Receptionist`, or `Medical_Records`.

| Role | Example routes |
|------|----------------|
| Clinical | `GET/PUT /patients`, `GET /patients/{id}` |
| Receptionist | `POST /appointments`, `PUT /appointments/{id}/attendance`, `GET /appointments/missed?date=YYYY-MM-DD`, `GET /appointments/pending-records` |
| Medical_Records | `GET /reports/patients-per-clinic`, `GET /reports/prescription-stats`, `POST /reports/change-requests` |

## Data model

Conceptual entity-relationship view (some tables, e.g. `patient_condition` and `comment`, are modeled in code but not all are present in the shipped MySQL DDL—extend `schema.sql` if you need them in the database).

```mermaid
erDiagram
    USER ||--o{ APPOINTMENT : manages_or_attends
    USER ||--o{ PRESCRIPTION : issues
    USER ||--o{ WARNING_OVERRIDE : authorizes
    USER ||--o{ COMMENT : writes
    PATIENT ||--o{ APPOINTMENT : attends
    PATIENT ||--o{ PATIENT_CONDITION : has
    PATIENT ||--o{ INCIDENT : involved_in
    PATIENT ||--o{ ADVERSE_REACTION : suffers_from
    PATIENT ||--o{ COMMENT : receives
    CLINIC ||--o{ APPOINTMENT : hosts
    APPOINTMENT ||--o{ PRESCRIPTION : results_in
    CONDITION ||--o{ PATIENT_CONDITION : describes
    MEDICATION ||--o{ PRESCRIPTION : contains
    MEDICATION ||--o{ ADVERSE_REACTION : causes
    MEDICATION ||--o{ WARNING_OVERRIDE : triggers

    USER {
        int user_id PK
        string username
        string password_hash
        string role
    }

    PATIENT {
        int patient_id PK
        string first_name
        string last_name
        string address
        boolean is_homeless
        string risk_status
        boolean is_deceased
        boolean self_harm_history
    }

    CLINIC {
        int clinic_id PK
        string name
        string location_type
    }

    APPOINTMENT {
        int appointment_id PK
        int patient_id FK
        int clinic_id FK
        int staff_id FK
        date appointment_date
        string type
        string status
        boolean records_updated
    }

    CONDITION {
        int condition_id PK
        string name
    }

    PATIENT_CONDITION {
        int patient_id FK
        int condition_id FK
        date diagnosis_date
    }

    MEDICATION {
        int medication_id PK
        string name
    }

    PRESCRIPTION {
        int prescription_id PK
        int appointment_id FK
        int medication_id FK
        int prescriber_id FK
        date issue_date
        boolean is_repeat
    }

    INCIDENT {
        int incident_id PK
        int patient_id FK
        string type
        string description
        date incident_date
    }

    ADVERSE_REACTION {
        int reaction_id PK
        int patient_id FK
        int medication_id FK
        string description
    }

    WARNING_OVERRIDE {
        int override_id PK
        int prescriber_id FK
        int medication_id FK
        string warning_details
        date override_date
    }

    COMMENT {
        int comment_id PK
        int patient_id FK
        int clinician_id FK
        string free_form_text
        date comment_date
    }

    CHANGE_REQUEST {
        int request_id PK
        string raw_patient_data
        string requested_changes
        string status
    }
```

**Field notes (domain semantics)**

- **USER.role:** `Clinical`, `Receptionist`, `Medical_Records`.  
- **CLINIC.location_type:** e.g. Hospital, Health Centre.  
- **APPOINTMENT.type:** e.g. Drop-in, Pre-arranged. **APPOINTMENT.status:** e.g. Attended, Missed, Pending.  
- **INCIDENT.type:** e.g. Deliberate, Accidental.  
- **CHANGE_REQUEST.status:** e.g. Pending, Accepted, Rejected. Change requests store raw payloads only and do not reference `patient_id` in the implemented DAO.

## Project layout

| Area | Package / path |
|------|----------------|
| REST resources | `com.skillonnet.automation.api` |
| JDBC DAOs | `com.skillonnet.automation.dao` |
| Domain models | `com.skillonnet.automation.model` |
| Clinical rules (prescriptions / incidents) | `com.skillonnet.automation.service` |
| MySQL DDL | `src/main/resources/schema.sql` |

## License

This project is provided as-is for coursework or demonstration unless you add a separate license.
