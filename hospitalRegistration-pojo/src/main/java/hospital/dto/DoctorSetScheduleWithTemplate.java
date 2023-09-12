package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSetScheduleWithTemplate {

    //使用的模板id
    private Long templateId;

    //被设置的日期
    private Date date;
}
