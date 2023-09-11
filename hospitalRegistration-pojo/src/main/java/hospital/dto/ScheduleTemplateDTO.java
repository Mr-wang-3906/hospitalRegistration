package hospital.dto;

import hospital.entity.RegistrationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleTemplateDTO {

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

    //挂号种类备选项
    private List<Long> registrationTypes_Ids;

}
