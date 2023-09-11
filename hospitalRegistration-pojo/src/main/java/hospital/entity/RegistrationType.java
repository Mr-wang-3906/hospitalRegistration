package hospital.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationType {

    //id
    private Long id;

    //挂号名称
    private String registrationName;

    //挂号费用
    private String count;

    //预计时长
    private String estimatedTime;
}
