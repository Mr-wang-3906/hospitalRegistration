package hospital.vo;

import hospital.entity.RegistrationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient_Doctor_SchedulingVO {

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

    //患者选择的挂号种类
    private List<RegistrationType> registrationTypes;

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
