package hospital.controller.doctor;

import hospital.context.BaseContext;
import hospital.dto.*;
import hospital.entity.Patient;
import hospital.entity.RegistrationType;
import hospital.result.Result;
import hospital.service.DoctorService;
import hospital.temp.DoctorInfo;
import hospital.temp.Doctor_SchedulingTemp;
import hospital.temp.PatientAppointmentInfo;
import hospital.vo.Doctor_SchedulingVO;
import hospital.vo.ScheduleTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@Slf4j
@Api(tags = "医生端")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    public static String verCode = "";

    /**
     * 医生注册(他人注册)
     **/
    @PostMapping("/registerOther")
    @ApiOperation(value = "医生注册(他人注册)")
    public Result doctorRegisterOther(@RequestBody DoctorRegisterDTO doctorRegisterDTO, HttpServletRequest httpServletRequest) {
        doctorService.insertNewDoctor(doctorRegisterDTO, httpServletRequest);
        return Result.success();
    }

    /**
     * 查询医生信息
     */
    @GetMapping("/queryInfo")
    @ApiOperation(value = "查询医生信息")
    public Result<DoctorInfo> doctorQueryInfo() {
        DoctorInfo doctorInfo = doctorService.queryInfoById(BaseContext.getCurrentId());
        return Result.success(doctorInfo);
    }

    /**
     * 修改医生信息
     */
    @PostMapping("/updateInfo")
    @ApiOperation(value = "修改医生信息")
    public Result doctorUpdateInfo(@RequestBody DoctorInfo doctorInfo) {
        doctorService.updateInfo(doctorInfo);
        return Result.success();
    }

    /**
     * 新增挂号种类
     */
    @PostMapping("/registration/add")
    @ApiOperation(value = "新增挂号种类")
    public Result doctorAddRegistration(@RequestBody RegistrationType registrationType) {
        doctorService.addRegistrationType(registrationType);
        return Result.success();
    }

    /**
     * 查询挂号种类
     */
    @GetMapping("/registration/query")
    @ApiOperation(value = "查询挂号种类")
    public Result<List<RegistrationType>> doctorQueryRegistration() {
        List<RegistrationType> registrationTypes = doctorService.queryRegistrationType();
        return Result.success(registrationTypes);
    }

    /**
     * 修改挂号种类
     */
    @PostMapping("/registration/update")
    @ApiOperation("修改挂号种类")
    public Result doctorUpdateRegistration(@RequestBody RegistrationType registrationType) {
        doctorService.updateRegistrationType(registrationType);
        return Result.success();
    }

    /**
     * 删除挂号种类
     */
    @PostMapping("/registration/delete")
    @ApiOperation("删除挂号种类")
    public Result doctorDeleteRegistration(@RequestBody List<Long> ids) {
        doctorService.deleteRegistrationType(ids);
        return Result.success();
    }

    /**
     * 新增排班模板
     */
    @PostMapping("/template/add")
    @ApiOperation(value = "新增排班模板")
    public Result doctorAddTemplate(@RequestBody ScheduleTemplateDTO scheduleTemplateDTO) {
        doctorService.addTemplate(scheduleTemplateDTO);
        return Result.success();
    }

    /**
     * 查询排班模板
     */
    @GetMapping("/template/query")
    @ApiOperation(value = "查询排班模板")
    public Result<List<ScheduleTemplateVO>> doctorQueryTemplates() {
        List<ScheduleTemplateVO> scheduleTemplates = doctorService.queryTemplate(BaseContext.getCurrentId());
        return Result.success(scheduleTemplates);
    }

    /**
     * 删除排班模板
     */
    @PostMapping("/template/delete")
    @ApiOperation(value = "删除排班模板")
    public Result doctorDeleteTemplates(@RequestBody List<Long> ids) {
        doctorService.deleteTemplates(ids);
        return Result.success();
    }

    /**
     * 修改排班模板
     */
    @PostMapping("/template/update")
    @ApiOperation(value = "修改排班模板")
    public Result doctorUpdateTemplate(@RequestBody ScheduleTemplateDTO scheduleTemplateDTO) {
        doctorService.updateTemplate(scheduleTemplateDTO);
        return Result.success();
    }

    /**
     * 排班信息查询
     */
    @GetMapping("/schedule/query")
    @ApiOperation("排班信息查询")
    public Result<List<Doctor_SchedulingVO>> doctorQuerySchedule() {
        List<Doctor_SchedulingVO> doctorSchedules = doctorService.querySchedule(BaseContext.getCurrentId());
        return Result.success(doctorSchedules);
    }

    /**
     * 模板安排某天排班
     */
    @PostMapping("/schedule/template")
    @ApiOperation(value = "使用模板为某天排班")
    public Result doctorSetScheduleWithTemplate(@RequestBody DoctorSetScheduleWithTemplate doctorSetScheduleWithTemplate) {
        doctorService.setScheduleWithTemplate(doctorSetScheduleWithTemplate);
        return Result.success();
    }

    /**
     * 单独修改某日排班
     */
    @PostMapping("/schedule/set")
    @ApiOperation("单独修改某日排班")
    public Result doctorSetOneDaySchedule(@RequestBody Doctor_SchedulingTemp doctorSchedulingTemp) {
        doctorService.updateOneDaySchedule(doctorSchedulingTemp);
        return Result.success();
    }

//    /**
//     * 排班复制-周复制 (废弃)
//     */
//    @PostMapping("/schedule/copy/week")
//    @ApiOperation(value = "排班复制-周复制")
//    public Result scheduleCopyWeek(@RequestBody ScheduleCopy_week scheduleCopyWeek) {
//        doctorService.copyScheduleWeek(scheduleCopyWeek);
//        return Result.success();
//

    /**
     * 排班复制-周复制
     */
    @PostMapping("/schedule/copy/week")
    @ApiOperation(value = "排班复制-周复制")
    public Result scheduleCopyWeek(@RequestBody ScheduleCopy_week scheduleCopyWeek) {
        doctorService.copyScheduleWeek(scheduleCopyWeek);
        return Result.success();
    }

    /**
     * 排班复制-月复制
     */
    @PostMapping("/schedule/copy/month")
    @ApiOperation(value = "排班复制-月复制")
    public Result scheduleCopyMonth(@RequestBody ScheduleCopy_month scheduleCopyMonth) {
        doctorService.copyScheduleMonth(scheduleCopyMonth);
        return Result.success();
    }

    /**
     * 排班复制-日复制
     */
    @PostMapping("/schedule/copy/day")
    @ApiOperation(value = "排班复制-日复制")
    public Result scheduleCopyDay(@RequestBody ScheduleCopy_day scheduleCopyDay) {
        doctorService.copyScheduleDay(scheduleCopyDay);
        return Result.success();
    }

    /**
     * 放号
     */
    @GetMapping("/registration/deliver")
    @ApiOperation(value = "提前放号")
    public Result doctorDeliverRegistration(){
        doctorService.deliverRegistration();
        return Result.success();
    }

    /**
     * 查看某日每时段的挂号状态
     */
    @GetMapping("/registration/check/{date}")
    @ApiOperation("查看某日每时段的挂号状态")
    public Result<List<PatientAppointmentInfo>> doctorRegistrationCheck(@PathVariable String date){
        List<PatientAppointmentInfo> patients = doctorService.registrationCheck(date);
        return Result.success(patients);
    }

    /**
     * 设置患者是否失约
     */
    @PostMapping("/setPatientCredit")
    @ApiOperation("设置患者是否失约")
    public Result setPatientCredit(@RequestBody PatientAppointmentInfoDTO patientAppointmentInfoDTO){
        doctorService.setPatientCredit(patientAppointmentInfoDTO);
        return Result.success();
    }
}
