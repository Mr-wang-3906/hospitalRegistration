package hospital.temp;

import lombok.Data;


@Data
public class Orders {

    //订单的那一天
    private String date;

    //医生id
    private Long doctorId;

    //选择的科室
    private String section;

    //选择的挂号种类id
    private Long registrationTypeId;

    //选择时间
    private String choiceTime;

    //单号
    private String oddNumber;
}
