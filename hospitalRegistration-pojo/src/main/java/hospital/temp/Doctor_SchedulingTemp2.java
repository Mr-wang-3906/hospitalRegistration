package hospital.temp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor_SchedulingTemp2 {

    //医生id
    private Long doctorId;

    //日期(string形式)
    private String data;

    //上午问诊患者人数
    private int morningCheckNumber;

    //下午问诊患者人数
    private int afternoonCheckNumber;

    //挂号种类id
    private String registrationTypeIds;
}
