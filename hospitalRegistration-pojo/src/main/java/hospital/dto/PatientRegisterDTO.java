package hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegisterDTO {
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

    //验证码
    private String verify;

    //邮箱地址
    private String emailAddress;
}
