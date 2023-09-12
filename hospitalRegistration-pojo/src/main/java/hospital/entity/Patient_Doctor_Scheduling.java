package hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient_Doctor_Scheduling {

    //id
    private Long id;

    //医生id
    private Long doctorId;

    //挂号总数-上午
    private int registrationNumber_morning;

    //挂号总数-下午
    private int registrationNumber_afternoon;

    //日期
    private Date date;

    //患者选择的挂号种类id
    private Long registrationTypeId;

    //9:00-10:00剩余挂号时长
    private int nine_ten;

    //10:00-11:00剩余挂号时长
    private int ten_eleven;

    //11:00-12:00剩余挂号时长
    private int eleven_twelve;

    //14:00-15:00剩余挂号时长
    private int fourteen_fifteen;

    //15:00-16:00剩余挂号时长
    private int fifteen_sixteen;

    //16:00-17:00剩余挂号时长
    private int sixteen_seventeen;


}
