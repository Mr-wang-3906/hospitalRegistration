package hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
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

    //失约次数
    private Integer NoShowNumber;
}
