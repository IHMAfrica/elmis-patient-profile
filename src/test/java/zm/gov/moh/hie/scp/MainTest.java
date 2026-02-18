package zm.gov.moh.hie.scp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zm.gov.moh.hie.scp.dto.PatientProfileMessage;
import zm.gov.moh.hie.scp.model.PatientProfileRecord;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testValidPatientProfileParsing() throws Exception {
        String json = "{\n" +
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

        // Parse JSON
        PatientProfileMessage msg = mapper.readValue(json, PatientProfileMessage.class);

        // Verify MSH fields
        assertNotNull(msg);
        assertNotNull(msg.msh);
        assertEquals("5b36fb1a-0f84-48f6-9af8-425b4654ff78", msg.msh.messageId);
        assertEquals("60070012", msg.msh.hmisCode);
        assertEquals("60070012", msg.msh.mflCode);
        assertEquals("CarePro", msg.msh.sendingApplication);
        assertEquals("elmis", msg.msh.receivingApplication);
        assertEquals("profile", msg.msh.messageType);
        assertEquals("2026-02-16 16:26:04", msg.msh.timestamp);

        // Verify patient fields
        assertEquals("2023-06-15 00:00:00", msg.registrationDateTime);
        assertEquals("1993-10-26", msg.dateOfBirth);
        assertEquals("39e1224a-b701-4113-1950-08de6d521958", msg.patientUuid);
        assertEquals("000000/00/0", msg.nrcNumber);
        assertEquals("ALLENT", msg.firstName);
        assertEquals("KANYANTA", msg.lastName);
        assertEquals("6007-0012C-21285472-1", msg.patientId);
        assertEquals("M", msg.sex);

        // Create record
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
                msg.sex
        );

        // Verify record
        assertNotNull(record);
        assertEquals("5b36fb1a-0f84-48f6-9af8-425b4654ff78", record.messageId);
        assertEquals("60070012", record.hmisCode);
        assertEquals("60070012", record.mflCode);
        assertEquals("ALLENT", record.firstName);
        assertEquals("KANYANTA", record.lastName);
        assertEquals("M", record.sex);

        System.out.println("[OK] Test PASSED: Valid patient profile parsed correctly");
    }

    @Test
    public void testProfileWithMissingOptionalFields() throws Exception {
        String json = "{\n" +
                "  \"msh\": {\n" +
                "    \"timestamp\": \"2026-02-16 16:26:04\",\n" +
                "    \"messageId\": \"profile-123\"\n" +
                "  },\n" +
                "  \"firstName\": \"John\",\n" +
                "  \"lastName\": \"Doe\"\n" +
                "}";

        PatientProfileMessage msg = mapper.readValue(json, PatientProfileMessage.class);

        assertNotNull(msg);
        assertEquals("profile-123", msg.msh.messageId);
        assertNull(msg.msh.hmisCode);
        assertNull(msg.dateOfBirth);
        assertEquals("John", msg.firstName);
        assertEquals("Doe", msg.lastName);

        System.out.println("[OK] Test PASSED: Profile with missing optional fields handled correctly");
    }

    @Test
    public void testInvalidJson() {
        String invalidJson = "{invalid json}";

        assertThrows(Exception.class, () -> {
            mapper.readValue(invalidJson, PatientProfileMessage.class);
        });

        System.out.println("[OK] Test PASSED: Invalid JSON throws exception as expected");
    }

    @Test
    public void testNullMessageId() throws Exception {
        String json = "{\n" +
                "  \"msh\": {\n" +
                "    \"timestamp\": \"2026-02-16 16:26:04\"\n" +
                "  },\n" +
                "  \"firstName\": \"Test\"\n" +
                "}";

        PatientProfileMessage msg = mapper.readValue(json, PatientProfileMessage.class);
        String messageId = msg.msh != null ? msg.msh.messageId : null;

        assertNull(messageId);
        System.out.println("[OK] Test PASSED: Null messageId handled correctly");
    }

    @Test
    public void testFilterCondition() throws Exception {
        String json = "{\n" +
                "  \"msh\": {\n" +
                "    \"timestamp\": \"2026-02-16 16:26:04\",\n" +
                "    \"messageId\": \"profile-456\"\n" +
                "  }\n" +
                "}";

        PatientProfileMessage msg = mapper.readValue(json, PatientProfileMessage.class);
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
                msg.sex
        );

        // Simulate filter condition: record != null && !isNullOrWhitespaceOnly(record.messageId)
        boolean shouldPass = record != null && record.messageId != null && !record.messageId.isBlank();
        assertTrue(shouldPass);

        System.out.println("[OK] Test PASSED: Filter condition works correctly");
    }
}
