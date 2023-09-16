package hospital.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePasswordDTO implements Serializable {

    //旧密码
    private String old_password;

    //新密码
    private String new_password;
}
