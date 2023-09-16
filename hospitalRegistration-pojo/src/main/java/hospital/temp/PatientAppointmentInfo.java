package hospital.temp;

import hospital.vo.PatientAppiontmentPationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientAppointmentInfo {

    //预约时间
    private String AppointmentNumber;

    //患者信息
    private List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos;
}
