# elmis-patient-profile

Apache Flink streaming pipeline that consumes patient profile messages from Kafka and writes them to PostgreSQL.

## Overview

- **Source**: Kafka topic `patient-profiles`
- **Sink**: PostgreSQL table `crt.patient_profile`
- **Consumer Group**: `scpro-elmis-patient-profile-pipeline`

## Prerequisites

- JDK 17
- Gradle (included via wrapper)
- PostgreSQL database with `crt.patient_profile` table

## Build

```bash
./gradlew clean shadowJar
```

Output: `build/libs/elmis-patient-profile-all.jar`

## Run Locally

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC=patient-profiles
export JDBC_URL=jdbc:postgresql://localhost:5432/hie_manager
export JDBC_USER=postgres
export JDBC_PASSWORD=postgres

java -jar build/libs/elmis-patient-profile-all.jar
```

## Database Setup

Create the table:

```sql
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
```

Or use the migration script:

```bash
psql -U postgres -d hie_manager -f db_migrations/create_patient_profile_table.sql
```

## Testing

Run unit tests:
```bash
./gradlew test
```

## Kubernetes Deployment

Deploy using FlinkSessionJob:

```bash
kubectl apply -f k8s/fleet/flink-sessionjob.yaml
```

## Configuration

Environment variables override defaults:
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap servers
- `KAFKA_TOPIC` - Topic to consume (default: `patient-profiles`)
- `KAFKA_GROUP_ID` - Consumer group (default: `scpro-elmis-patient-profile-pipeline`)
- `KAFKA_SECURITY_PROTOCOL` - SASL_PLAINTEXT or PLAINTEXT
- `KAFKA_SASL_MECHANISM` - SCRAM-SHA-512
- `KAFKA_SASL_USERNAME` - Kafka username
- `KAFKA_SASL_PASSWORD` - Kafka password
- `JDBC_URL` - PostgreSQL connection URL
- `JDBC_USER` - Database user
- `JDBC_PASSWORD` - Database password
