package hospital.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientAppiontmentPationInfo {
    //预约id
    private Long appointmentId;

    //患者id
    private Long patientId;

    //用户名
    private String userName;

    //姓名
    private String name;

    //年龄
    private String age;

    //性别
    private String gender;

    //失约次数
    private Integer NoShowNumber;

    //挂号状态
    private String AppointmentStatus;

    //挂号类型-名称
    private String registrationName;
}
