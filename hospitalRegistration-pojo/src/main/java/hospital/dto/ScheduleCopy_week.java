package hospital.dto;

import lombok.Data;

@Data
public class ScheduleCopy_week {

    //复制源
    private String sourWeekNumber;

    //目标日期
    private String targetWeekNumber;
}
