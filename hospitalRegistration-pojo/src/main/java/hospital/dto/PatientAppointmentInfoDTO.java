package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAppointmentInfoDTO {
    //患者
    private Long patientId;

    //预约时间
    private String registrationTime;

    //科室
    private String section;

    //完成or失约
    private String status;
}
