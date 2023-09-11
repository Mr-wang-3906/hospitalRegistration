package hospital.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hospital.jwt")
@Data
public class JwtProperties {

    /**
     * 教师端生成jwt令牌相关配置
     */
    private String doctorSecretKey;
    private long doctorTtl;
    private String doctorTokenName;

    /**
     * 学生端用户生成jwt令牌相关配置
     */
    private String patientSecretKey;
    private long patientTtl;
    private String patientTokenName;

}
