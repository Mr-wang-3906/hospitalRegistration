package hospital.vo;

import hospital.entity.RegistrationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor_SchedulingVO {

    //id
    private Long id;

    //医生id
    private Long doctorId;

    //日期
    private Date data;

    //上午问诊患者人数
    private int morningCheckNumber;

    //下午问诊患者人数
    private int afternoonCheckNumber;

    //挂号种类id
    private String registrationTypeIds;
}
