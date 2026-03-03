package zm.gov.moh.hie.scp.sink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import zm.gov.moh.hie.scp.model.PatientProfileRecord;
import zm.gov.moh.hie.scp.util.DateTimeUtil;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostgresPatientProfileSink extends RichSinkFunction<PatientProfileRecord> {
    private final String jdbcUrl;
    private final String user;
    private final String password;
    private final String table;

    private transient Connection connection;
    private transient PreparedStatement insertStmt;

    public PostgresPatientProfileSink(String jdbcUrl, String user, String password, String table) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
        this.table = table;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        connection = DriverManager.getConnection(jdbcUrl, user, password);
        connection.setAutoCommit(true);
        String insertQuery = "INSERT INTO " + table + "(" +
                "message_id, hmis_code, mfl_code, sending_application, receiving_application, message_type, " +
                "registration_date_time, date_of_birth, patient_uuid, sex, date, \"time\") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        insertStmt = connection.prepareStatement(insertQuery);
    }

    @Override
    public void invoke(PatientProfileRecord value, Context context) throws Exception {
        insertStmt.setString(1, value.messageId);
        insertStmt.setString(2, value.hmisCode);
        insertStmt.setString(3, value.mflCode);
        insertStmt.setString(4, value.sendingApplication);
        insertStmt.setString(5, value.receivingApplication);
        insertStmt.setString(6, value.messageType);

        // Parse registrationDateTime (datetime format)
        if (value.registrationDateTime != null && !value.registrationDateTime.isBlank()) {
            LocalDateTime regDateTime = LocalDateTime.parse(value.registrationDateTime, DateTimeUtil.TIMESTAMP_FORMATTER);
            insertStmt.setTimestamp(7, Timestamp.valueOf(regDateTime));
        } else {
            insertStmt.setNull(7, Types.TIMESTAMP);
        }

        // Parse dateOfBirth (date-only format)
        if (value.dateOfBirth != null && !value.dateOfBirth.isBlank()) {
            LocalDate dob = LocalDate.parse(value.dateOfBirth, DateTimeUtil.DATE_FORMATTER);
            insertStmt.setDate(8, java.sql.Date.valueOf(dob));
        } else {
            insertStmt.setNull(8, Types.DATE);
        }

        insertStmt.setString(9, value.patientUuid);
        insertStmt.setString(10, value.sex);

        // Parse mshTimestamp and split into date/time (for audit trail)
        LocalDateTime timestamp = LocalDateTime.parse(value.mshTimestamp, DateTimeUtil.TIMESTAMP_FORMATTER);
        Timestamp ts = Timestamp.valueOf(timestamp);
        insertStmt.setDate(11, new java.sql.Date(ts.getTime()));
        insertStmt.setTime(12, new java.sql.Time(ts.getTime()));

        insertStmt.executeUpdate();
    }

    @Override
    public void close() throws Exception {
        if (insertStmt != null) insertStmt.close();
        if (connection != null) connection.close();
    }
}
