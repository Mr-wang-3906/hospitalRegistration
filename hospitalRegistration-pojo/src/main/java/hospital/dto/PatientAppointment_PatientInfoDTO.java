package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAppointment_PatientInfoDTO {

    //预约id
    private Long appointmentId;

    //患者id
    private Long patientId;

    //姓名
    private String name;

    //性别
    private String gender;

    //年龄
    private String age;

    //完成or失约
    private String status;
}
