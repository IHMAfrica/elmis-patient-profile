CREATE SCHEMA IF NOT EXISTS crt;

CREATE TABLE IF NOT EXISTS crt.patient_profile (
    id                   BIGSERIAL PRIMARY KEY,
    message_id           VARCHAR(255),
    hmis_code            VARCHAR(50),
    mfl_code             VARCHAR(50),
    sending_application  VARCHAR(100),
    receiving_application VARCHAR(100),
    message_type         VARCHAR(50),
    registration_date_time TIMESTAMP,
    date_of_birth        DATE,
    patient_uuid         VARCHAR(255),
    nrc_number           VARCHAR(100),
    first_name           VARCHAR(255),
    last_name            VARCHAR(255),
    patient_id           VARCHAR(255),
    sex                  VARCHAR(10),
    date                 DATE,
    "time"               TIME,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_patient_profile_message_id ON crt.patient_profile(message_id);
CREATE INDEX IF NOT EXISTS idx_patient_profile_patient_uuid ON crt.patient_profile(patient_uuid);
CREATE INDEX IF NOT EXISTS idx_patient_profile_patient_id ON crt.patient_profile(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_profile_date ON crt.patient_profile(date);
