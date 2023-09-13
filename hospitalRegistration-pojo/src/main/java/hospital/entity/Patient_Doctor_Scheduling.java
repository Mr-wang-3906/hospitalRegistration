package hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient_Doctor_Scheduling {

    //id
    private Long id;

    //医生id
    private Long doctorId;

    //挂号总数-上午
    private int registrationNumberMorning;

    //挂号总数-下午
    private int registrationNumberAfternoon;

    //日期
    private String date;

    //患者选择的挂号种类id
    private String registrationTypeId;

    //9:00-10:00剩余挂号时长
    private int nineTen;

    //10:00-11:00剩余挂号时长
    private int tenEleven;

    //11:00-12:00剩余挂号时长
    private int elevenTwelve;

    //14:00-15:00剩余挂号时长
    private int fourteenFifteen;

    //15:00-16:00剩余挂号时长
    private int fifteenSixteen;

    //16:00-17:00剩余挂号时长
    private int sixteenSeventeen;


}
