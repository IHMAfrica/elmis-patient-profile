package zm.gov.moh.hie.scp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientProfileMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("msh")
    public Msh msh;

    @JsonProperty("registrationDateTime")
    public String registrationDateTime;

    @JsonProperty("dateOfBirth")
    public String dateOfBirth;

    @JsonProperty("patientUuid")
    public String patientUuid;

    @JsonProperty("nrcNumber")
    public String nrcNumber;

    @JsonProperty("firstName")
    public String firstName;

    @JsonProperty("lastName")
    public String lastName;

    @JsonProperty("patientId")
    public String patientId;

    @JsonProperty("sex")
    public String sex;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Msh implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("timestamp")
        public String timestamp;

        @JsonProperty("sendingApplication")
        public String sendingApplication;

        @JsonProperty("receivingApplication")
        public String receivingApplication;

        @JsonProperty("messageId")
        public String messageId;

        @JsonProperty("hmisCode")
        public String hmisCode;

        @JsonProperty("messageType")
        public String messageType;

        @JsonProperty("mflCode")
        public String mflCode;
    }
}
