package hospital.temp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor_SchedulingTemp1 {

    //医生id
    private Long doctorId;

    //日期
    private Date data;

    //医生某天排班使用的包含某特定挂号种类的挂号种类总数
    private Integer OneRegistrationTypeNumber;
}
