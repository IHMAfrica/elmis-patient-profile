package zm.gov.moh.hie.scp.model;

import java.io.Serializable;

public class PatientProfileRecord implements Serializable {
    public String messageId;
    public String hmisCode;
    public String mflCode;
    public String sendingApplication;
    public String receivingApplication;
    public String messageType;
    public String mshTimestamp;
    public String registrationDateTime;
    public String dateOfBirth;
    public String patientUuid;
    public String nrcNumber;
    public String firstName;
    public String lastName;
    public String patientId;
    public String sex;

    public PatientProfileRecord() {}

    public PatientProfileRecord(String messageId, String hmisCode, String mflCode,
                                String sendingApplication, String receivingApplication, String messageType,
                                String mshTimestamp, String registrationDateTime, String dateOfBirth,
                                String patientUuid, String nrcNumber, String firstName, String lastName,
                                String patientId, String sex) {
        this.messageId = messageId;
        this.hmisCode = hmisCode;
        this.mflCode = mflCode;
        this.sendingApplication = sendingApplication;
        this.receivingApplication = receivingApplication;
        this.messageType = messageType;
        this.mshTimestamp = mshTimestamp;
        this.registrationDateTime = registrationDateTime;
        this.dateOfBirth = dateOfBirth;
        this.patientUuid = patientUuid;
        this.nrcNumber = nrcNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patientId = patientId;
        this.sex = sex;
    }
}
