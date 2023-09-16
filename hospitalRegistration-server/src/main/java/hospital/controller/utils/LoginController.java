package hospital.controller.utils;

import hospital.constant.JwtClaimsConstant;
import hospital.constant.MessageConstant;
import hospital.context.BaseContext;
import hospital.dto.LoginDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.exception.LoginFailedException;
import hospital.properties.JwtProperties;
import hospital.result.Result;
import hospital.service.DoctorService;
import hospital.service.PatientService;
import hospital.utils.JwtUtil;
import hospital.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
@Slf4j
@Api(tags = "登录")
public class LoginController {
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PatientService patientService;

    /**
     * 用户登录
     **/
    @PostMapping
    @ApiOperation(value = "用户登录")
    public Result<LoginVO> doctorLogin(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录：{}", loginDTO);


        Doctor doctor = doctorService.login(loginDTO);
        Patient patient = patientService.login(loginDTO);

        if (doctor == null && patient == null) {
            throw new LoginFailedException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        Map<String, Object> claims = new HashMap<>();
        if (doctor != null) {

            //登录成功后，生成jwt令牌
            claims.put(JwtClaimsConstant.EMP_ID, doctor.getId());
            String token = JwtUtil.createJWT(
                    jwtProperties.getDoctorSecretKey(),
                    jwtProperties.getDoctorTtl(),
                    claims);

            LoginVO loginVO = LoginVO.builder()
                    .id(doctor.getId())
                    .userName(doctor.getUserName())
                    .name(doctor.getName())
                    .token(token)
                    .identity("医生")
                    .build();
            //将teacherId存入LocalThread中
            BaseContext.setCurrentId(doctor.getId());
            return Result.success(loginVO);
        }else {
            //登录成功后，生成jwt令牌
            claims.put(JwtClaimsConstant.EMP_ID, patient.getId());
            String token = JwtUtil.createJWT(
                    jwtProperties.getDoctorSecretKey(),
                    jwtProperties.getDoctorTtl(),
                    claims);

            LoginVO loginVO = LoginVO.builder()
                    .id(patient.getId())
                    .userName(patient.getUserName())
                    .name(patient.getName())
                    .token(token)
                    .identity("患者")
                    .build();
            //将teacherId存入LocalThread中
            BaseContext.setCurrentId(patient.getId());
            return Result.success(loginVO);
        }
    }
}
