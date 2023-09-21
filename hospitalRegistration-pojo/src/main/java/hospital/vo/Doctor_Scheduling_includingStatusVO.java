package hospital.vo;

import hospital.entity.RegistrationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor_Scheduling_includingStatusVO implements Serializable {

    //医生id
    private Long doctorId;

    //日期
    private String date;

    //是否放号 0否 1是
    private Integer deliverOrNot;

    //上午问诊患者人数
    private int morningCheckNumber;

    //下午问诊患者人数
    private int afternoonCheckNumber;

    //挂号种类id
    private List<RegistrationType> registrationTypes;
}
