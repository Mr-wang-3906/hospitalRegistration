package hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    //id
    private Long id;

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
}
