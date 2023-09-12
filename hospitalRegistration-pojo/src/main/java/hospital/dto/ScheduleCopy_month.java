package hospital.dto;

import lombok.Data;

@Data
public class ScheduleCopy_month {

    //复制源 月
    private String sourMonthNumber;

    //目标  月
    private String targetMonthNumber;
}
