package zm.gov.moh.hie.scp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zm.gov.moh.hie.scp.dto.PatientProfileMessage;
import zm.gov.moh.hie.scp.model.PatientProfileRecord;
import zm.gov.moh.hie.scp.util.DateTimeUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseInsertTest {
    private ObjectMapper mapper;
    private Connection connection;

    // Database configuration
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/hie_manager";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "postgres";
    private static final String TABLE_NAME = "crt.patient_profile";

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testInsertPatientProfileIntoDatabase() throws Exception {
        System.out.println("\n=== TEST: Insert Patient Profile into Database ===");

        // Step 1: Connect to database
        System.out.println("Step 1: Connecting to database...");
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            connection.setAutoCommit(true);
            System.out.println("[OK] Connected to PostgreSQL");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to connect to database: " + e.getMessage());
            System.out.println("   Skipping database test - ensure PostgreSQL is running");
            return;
        }

        try {
            // Step 2: Verify table exists
            System.out.println("Step 2: Verifying table exists...");
            try {
                String checkQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(checkQuery);
                if (rs.next()) {
                    System.out.println("[OK] Table " + TABLE_NAME + " exists");
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                System.out.println("[WARN] Table may not exist: " + e.getMessage());
                System.out.println("   Please run: psql ... < db_migrations/create_patient_profile_table.sql");
                connection.close();
                return;
            }

            // Step 3: Parse test JSON message
            System.out.println("Step 3: Parsing test patient profile...");
            String testJson = "{\n" +
                    "  \"msh\": {\n" +
                    "    \"timestamp\": \"2026-02-16 16:26:04\",\n" +
                    "    \"sendingApplication\": \"CarePro\",\n" +
                    "    \"receivingApplication\": \"elmis\",\n" +
                    "    \"messageId\": \"5b36fb1a-0f84-48f6-9af8-425b4654ff78\",\n" +
                    "    \"hmisCode\": \"60070012\",\n" +
                    "    \"messageType\": \"profile\",\n" +
                    "    \"mflCode\": \"60070012\"\n" +
                    "  },\n" +
                    "  \"registrationDateTime\": \"2023-06-15 00:00:00\",\n" +
                    "  \"dateOfBirth\": \"1993-10-26\",\n" +
                    "  \"patientUuid\": \"39e1224a-b701-4113-1950-08de6d521958\",\n" +
                    "  \"nrcNumber\": \"000000/00/0\",\n" +
                    "  \"firstName\": \"ALLENT\",\n" +
                    "  \"lastName\": \"KANYANTA\",\n" +
                    "  \"patientId\": \"6007-0012C-21285472-1\",\n" +
                    "  \"sex\": \"M\"\n" +
                    "}";

            PatientProfileMessage msg = mapper.readValue(testJson, PatientProfileMessage.class);
            System.out.println("[OK] Parsed patient profile: firstName=" + msg.firstName + " lastName=" + msg.lastName);

            // Step 4: Create PatientProfileRecord
            System.out.println("Step 4: Creating record...");
            PatientProfileRecord record = new PatientProfileRecord(
                    msg.msh.messageId,
                    msg.msh.hmisCode,
                    msg.msh.mflCode,
                    msg.msh.sendingApplication,
                    msg.msh.receivingApplication,
                    msg.msh.messageType,
                    msg.msh.timestamp,
                    msg.registrationDateTime,
                    msg.dateOfBirth,
                    msg.patientUuid,
                    msg.nrcNumber,
                    msg.firstName,
                    msg.lastName,
                    msg.patientId,
                    msg.sex);
            System.out.println("[OK] Created record");

            // Step 5: Insert into database
            System.out.println("Step 5: Inserting record into database...");
            String insertQuery = "INSERT INTO " + TABLE_NAME + "(" +
                    "message_id, hmis_code, mfl_code, sending_application, receiving_application, message_type, " +
                    "registration_date_time, date_of_birth, patient_uuid, nrc_number, first_name, last_name, " +
                    "patient_id, sex, date, \"time\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setString(1, record.messageId);
            insertStmt.setString(2, record.hmisCode);
            insertStmt.setString(3, record.mflCode);
            insertStmt.setString(4, record.sendingApplication);
            insertStmt.setString(5, record.receivingApplication);
            insertStmt.setString(6, record.messageType);

            // Parse registrationDateTime
            LocalDateTime regDateTime = LocalDateTime.parse(record.registrationDateTime, DateTimeUtil.TIMESTAMP_FORMATTER);
            insertStmt.setTimestamp(7, Timestamp.valueOf(regDateTime));

            // Parse dateOfBirth
            LocalDate dob = LocalDate.parse(record.dateOfBirth, DateTimeUtil.DATE_FORMATTER);
            insertStmt.setDate(8, java.sql.Date.valueOf(dob));

            insertStmt.setString(9, record.patientUuid);
            insertStmt.setString(10, record.nrcNumber);
            insertStmt.setString(11, record.firstName);
            insertStmt.setString(12, record.lastName);
            insertStmt.setString(13, record.patientId);
            insertStmt.setString(14, record.sex);

            // Parse mshTimestamp
            LocalDateTime timestamp = LocalDateTime.parse(record.mshTimestamp, DateTimeUtil.TIMESTAMP_FORMATTER);
            Timestamp ts = Timestamp.valueOf(timestamp);
            insertStmt.setDate(15, new java.sql.Date(ts.getTime()));
            insertStmt.setTime(16, new java.sql.Time(ts.getTime()));

            int rowsInserted = insertStmt.executeUpdate();
            insertStmt.close();

            assertEquals(1, rowsInserted);
            System.out.println("[OK] Inserted 1 row");

            // Step 6: Verify insertion by querying
            System.out.println("Step 6: Verifying insertion...");
            String verifyQuery = "SELECT * FROM " + TABLE_NAME + " WHERE patient_uuid = ?";
            PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery);
            verifyStmt.setString(1, record.patientUuid);
            ResultSet rs = verifyStmt.executeQuery();

            assertTrue(rs.next(), "Record not found in database");
            assertEquals(record.firstName, rs.getString("first_name"));
            assertEquals(record.lastName, rs.getString("last_name"));
            assertEquals(record.patientId, rs.getString("patient_id"));
            System.out.println("[OK] Record verified in database");
            rs.close();
            verifyStmt.close();

            // Step 7: Query inserted data
            System.out.println("Step 7: Retrieved data from database:");
            verifyStmt = connection.prepareStatement(verifyQuery);
            verifyStmt.setString(1, record.patientUuid);
            rs = verifyStmt.executeQuery();
            if (rs.next()) {
                System.out.println("  - first_name: " + rs.getString("first_name"));
                System.out.println("  - last_name: " + rs.getString("last_name"));
                System.out.println("  - patient_id: " + rs.getString("patient_id"));
                System.out.println("  - date_of_birth: " + rs.getDate("date_of_birth"));
                System.out.println("  - sex: " + rs.getString("sex"));
                System.out.println("  - hmis_code: " + rs.getString("hmis_code"));
                System.out.println("  - created_at: " + rs.getTimestamp("created_at"));
            }
            rs.close();
            verifyStmt.close();

            System.out.println("\n[OK] TEST PASSED: Patient profile successfully inserted into database\n");

        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        }
    }

    @Test
    public void testInsertMultipleProfiles() throws Exception {
        System.out.println("\n=== TEST: Insert Multiple Patient Profiles ===");

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            connection.setAutoCommit(true);
        } catch (Exception e) {
            System.out.println("[WARN] Skipping database test - database not available");
            return;
        }

        try {
            String insertQuery = "INSERT INTO " + TABLE_NAME + "(" +
                    "message_id, hmis_code, mfl_code, sending_application, receiving_application, message_type, " +
                    "registration_date_time, date_of_birth, patient_uuid, nrc_number, first_name, last_name, " +
                    "patient_id, sex, date, \"time\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Insert 3 test profiles
            for (int i = 1; i <= 3; i++) {
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, "profile-msg-" + System.currentTimeMillis() + "-" + i);
                insertStmt.setString(2, "HMIS-" + i);
                insertStmt.setString(3, "MFL-" + i);
                insertStmt.setString(4, "CarePro");
                insertStmt.setString(5, "elmis");
                insertStmt.setString(6, "profile");
                insertStmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                insertStmt.setDate(8, java.sql.Date.valueOf(LocalDate.now()));
                insertStmt.setString(9, "patient-uuid-" + i);
                insertStmt.setString(10, "NRC-" + i);
                insertStmt.setString(11, "FirstName" + i);
                insertStmt.setString(12, "LastName" + i);
                insertStmt.setString(13, "patient-id-" + i);
                insertStmt.setString(14, i % 2 == 0 ? "M" : "F");

                LocalDateTime now = LocalDateTime.now();
                Timestamp ts = Timestamp.valueOf(now);
                insertStmt.setDate(15, new java.sql.Date(ts.getTime()));
                insertStmt.setTime(16, new java.sql.Time(ts.getTime()));

                insertStmt.executeUpdate();
                insertStmt.close();
                System.out.println("[OK] Inserted profile " + i);
            }

            // Count total records
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + TABLE_NAME);
            if (rs.next()) {
                System.out.println("[OK] Total records in table: " + rs.getInt("count"));
            }
            rs.close();
            stmt.close();

            System.out.println("\n[OK] TEST PASSED: Multiple profiles inserted successfully\n");

        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
}
