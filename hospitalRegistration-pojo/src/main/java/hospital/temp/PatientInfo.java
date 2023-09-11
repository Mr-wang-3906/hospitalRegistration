package hospital.temp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfo {

    //姓名
    private String name;

    //年龄
    private String age;

    //性别
    private String gender;
}
