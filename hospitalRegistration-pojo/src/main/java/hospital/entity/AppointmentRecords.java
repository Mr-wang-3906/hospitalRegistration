package hospital.entity;

import lombok.Data;

@Data
public class AppointmentRecords {

    private Long patientId;

    private String registrationTime;

    private String doctorName;

    private String registrationStatus;
}
