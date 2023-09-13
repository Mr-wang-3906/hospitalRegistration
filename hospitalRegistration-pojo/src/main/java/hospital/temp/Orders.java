package hospital.temp;

import lombok.Data;

import java.sql.Date;

@Data
public class Orders {

    //订单的那一天
    private Date date;

    //医生id
    private Long doctorId;

    //选择的挂号种类id
    private Long registrationTypeId;

    //选择时间
    private String choiceTime;
}
