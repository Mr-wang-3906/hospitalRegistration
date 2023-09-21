package hospital.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor_Scheduling_includingStatus {

    //id
    private Long id;

    //医生id
    private Long doctorId;

    //日期
    private Date data;

    //是否放号 0否 1是
    private Integer deliverOrNot;

    //上午问诊患者人数
    private int morningCheckNumber;

    //下午问诊患者人数
    private int afternoonCheckNumber;

    //挂号种类id
    private String registrationTypeIds;
}
