package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRegisterDTO {
    //用户名
    private String userName;

    //密码
    private String password;

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

    //验证码
    private String verify;
}
