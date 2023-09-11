package hospital.controller.patient;

import hospital.context.BaseContext;
import hospital.dto.PatientRegisterDTO;
import hospital.temp.PatientInfo;
import hospital.result.Result;
import hospital.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/patient")
@Slf4j
@Api(tags = "患者端")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 患者注册
     */
    @PostMapping("/register")
    @ApiOperation("患者注册")
    public Result patientRegister(@RequestBody PatientRegisterDTO patientRegisterDTO, HttpServletRequest httpServletRequest) {
        patientService.insertNewPatient(patientRegisterDTO,httpServletRequest);
        return Result.success();
    }

    /**
     * 查询患者信息
     */
    @GetMapping("/queryInfo")
    @ApiOperation(value = "查询患者信息")
    public Result<PatientInfo> patientQueryInfo(){
        PatientInfo patientInfo = patientService.queryInfo(BaseContext.getCurrentId());
        return Result.success(patientInfo);
    }
}
