package hospital.entity;

import lombok.Data;

@Data
public class AppointmentRecords {

    private Long id;

    private Long patientId;

    private String registrationTime;

    private String section;

    private Long registrationTypeId;

    private String doctorName;

    private String registrationStatus;

    private String oddNumber;
}
