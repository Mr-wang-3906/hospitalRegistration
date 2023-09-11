package hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTemplate {

    //id
    private Long id;

    //医生id
    private Long doctorId;

    //模板名称
    private String templateName;

    //上午挂号数量
    private int morningCheckNumber;

    //下午挂号数量
    private int afternoonCheckNumber;

    //挂号种类id
    private Long registrationTypeId;
}
