package hospital.temp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorInfo {

    //姓名
    private String name;

    //年龄
    private String age;

    //性别
    private String gender;

    //科室
    private String section;

    //擅长方向
    private String specializedField;

    //职位
    private String position;
}
