package hospital.controller.patient;

import hospital.context.BaseContext;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.AppointmentRecords;
import hospital.entity.Doctor;
import hospital.entity.Patient_Doctor_Scheduling;
import hospital.mapper.DoctorMapper;
import hospital.service.DoctorService;
import hospital.temp.Orders;
import hospital.temp.PatientInfo;
import hospital.result.Result;
import hospital.service.PatientService;
import hospital.vo.Patient_Doctor_SchedulingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/patient")
@Slf4j
@Api(tags = "患者端")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 患者注册
     */
    @PostMapping("/register")
    @ApiOperation("患者注册")
    public Result patientRegister(@RequestBody PatientRegisterDTO patientRegisterDTO) {
        patientService.insertNewPatient(patientRegisterDTO);
        return Result.success();
    }

    /**
     * 查询患者信息
     */
    @GetMapping("/queryInfo")
    @ApiOperation(value = "查询患者信息")
    public Result<PatientInfo> patientQueryInfo() {
        PatientInfo patientInfo = patientService.queryInfo(BaseContext.getCurrentId());
        return Result.success(patientInfo);
    }

    /**
     * 查看挂号界面
     */
    @PostMapping("/registration/check")
    @ApiOperation(value = "查看挂号界面")
    public Result<List<Doctor>> patientCheckRegistration(@RequestBody PatientCheckRegistrationDTO patientCheckRegistrationDTO) {
        List<Doctor> doctors = patientService.checkRegistration(patientCheckRegistrationDTO);
        return Result.success(doctors);
    }

    /**
     * 选择医生
     */
    @GetMapping("/choice/{doctorId}")
    @ApiOperation(value = "选择医生")
    public Result<List<Patient_Doctor_SchedulingVO>> choiceDoctor(@PathVariable Long doctorId) {
        List<Patient_Doctor_SchedulingVO> patientDoctorSchedulingVOs = patientService.choiceDoctor(doctorId);
        return Result.success(patientDoctorSchedulingVOs);
    }

    /**
     * 提交订单
     */
    @PostMapping("/choice/time")
    @ApiOperation(value = "提交订单")
    public Result choiceTime(@RequestBody Orders orders) {
        patientService.choiceTime(orders);
        return Result.success();
    }

    /**
     * 确认付款
     */
    @PostMapping("/confirmPayment")
    @ApiOperation(value = "确认付款")
    public Result confirmPayment(@RequestBody Orders orders){
        patientService.confirmPayment(orders);
            return Result.success();
    }

    /**
     * 付款功能
     */
    @PostMapping("/pay")
    @ApiOperation(value = "付款")
    public Result pay(){
        return Result.success();
    }

    /**
     * 取消预约
     */
    @PostMapping("/cancelPayment")
    @ApiOperation("取消预约")
    public Result cancelPayment(@RequestBody Orders orders){
        patientService.cancelPayment(orders);
        return Result.success();
    }

    /**
     * 查询历史预约信息
     */
    @GetMapping("/query/patient/appointment")
    @ApiOperation(value = "查询患者历史预约信息")
    public Result<List<AppointmentRecords>> queryPatientAppointment(){
        List<AppointmentRecords> appointmentRecordsList = doctorService.queryPatientAppointment(BaseContext.getCurrentId());
        return Result.success(appointmentRecordsList);
    }
}
