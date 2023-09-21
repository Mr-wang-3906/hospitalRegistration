package hospital.vo;

import hospital.entity.RegistrationType;
import lombok.Data;

import java.util.List;

@Data
public class AppointmentRecordsVO {

    private Long id;

    private String oddNumber;

    private Long patientId;

    private String registrationTime;

    private String section;

    private List<RegistrationType> registrationTypes;

    private String doctorName;

    private String registrationStatus;

    private Long lastTime;
}
