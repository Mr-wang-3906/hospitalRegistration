package hospital.temp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAppointmentInfo {
    //id
    private Long id;

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

    //预约时间
    private String AppointmentNumber;

    //挂号类型-名称
    private String registrationName;

    //挂号状态
    private String AppointmentStatus;
}
