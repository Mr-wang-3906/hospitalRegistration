package hospital.controller.doctor;

import hospital.context.BaseContext;
import hospital.dto.*;

import hospital.entity.RegistrationType;
import hospital.result.Result;
import hospital.service.DoctorService;
import hospital.temp.DoctorInfo;
import hospital.temp.Doctor_SchedulingTemp;
import hospital.temp.PatientAppointmentInfo;
import hospital.vo.AppointmentRecordsVO;
import hospital.vo.Doctor_Scheduling_includingStatus;
import hospital.vo.Doctor_Scheduling_includingStatusVO;
import hospital.vo.ScheduleTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/doctor")
@Slf4j
@Api(tags = "医生端")
public class DoctorController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DoctorService doctorService;


    /**
     * 医生注册(他人注册)
     **/
    @PostMapping("/registerOther")
    @ApiOperation(value = "医生注册(他人注册)")
    public Result doctorRegisterOther(@RequestBody DoctorRegisterDTO doctorRegisterDTO) {
        doctorService.insertNewDoctor(doctorRegisterDTO);
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
        cleanCache();
        doctorService.updateRegistrationType(registrationType);
        return Result.success();
    }

    /**
     * 删除挂号种类
     */
    @PostMapping("/registration/delete")
    @ApiOperation("删除挂号种类")
    public Result doctorDeleteRegistration(@RequestBody List<Long> ids) {
        cleanCache();
        doctorService.deleteRegistrationType(ids);
        return Result.success();
    }

    /**
     * 新增排班模板
     */
    @PostMapping("/template/add")
    @ApiOperation(value = "新增排班模板")
    public Result<Long> doctorAddTemplate(@RequestBody ScheduleTemplateDTO scheduleTemplateDTO) {
        Long newTemplateId = doctorService.addTemplate(scheduleTemplateDTO);
        return Result.success(newTemplateId);
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
    public Result<List<Doctor_Scheduling_includingStatusVO>> doctorQuerySchedule() {
        String key = "doctor_scheduling_" + BaseContext.getCurrentId();
        List<Object> redisList = redisTemplate.opsForList().range(key, 0, -1);
        ArrayList<Doctor_Scheduling_includingStatusVO> list = new ArrayList<>();

        if (redisList == null || redisList.isEmpty()) {
            // 如果不存在，查询数据库，将查询的数据放入Redis中
            list = doctorService.querySchedule(BaseContext.getCurrentId());
            if (list != null && !list.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, list.toArray());
            }
        } else {
            // 如果存在，直接使用Redis中的数据
            for (Object obj : redisList) {
                if (obj instanceof Doctor_Scheduling_includingStatusVO) {
                    list.add((Doctor_Scheduling_includingStatusVO) obj);
                }
            }
        }

        return Result.success(list);
    }

    /**
     * 模板安排某天排班
     */
    @PostMapping("/schedule/template")
    @ApiOperation(value = "使用模板为某天排班")
    public Result doctorSetScheduleWithTemplate(@RequestBody DoctorSetScheduleWithTemplate doctorSetScheduleWithTemplate) {
        cleanCache();
        doctorService.setScheduleWithTemplate(doctorSetScheduleWithTemplate);
        return Result.success();
    }

    /**
     * 单独修改某日排班
     */
    @PostMapping("/schedule/set")
    @ApiOperation("单独修改某日排班")
    public Result doctorSetOneDaySchedule(@RequestBody Doctor_SchedulingTemp doctorSchedulingTemp) {
        cleanCache();
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
        cleanCache();
        doctorService.copyScheduleWeek(scheduleCopyWeek);
        return Result.success();
    }

    /**
     * 排班复制-月复制
     */
    @PostMapping("/schedule/copy/month")
    @ApiOperation(value = "排班复制-月复制")
    public Result scheduleCopyMonth(@RequestBody ScheduleCopy_month scheduleCopyMonth) {
        cleanCache();
        doctorService.copyScheduleMonth(scheduleCopyMonth);
        return Result.success();
    }

    /**
     * 排班复制-日复制
     */
    @PostMapping("/schedule/copy/day")
    @ApiOperation(value = "排班复制-日复制")
    public Result scheduleCopyDay(@RequestBody ScheduleCopy_day scheduleCopyDay) {
        cleanCache();
        doctorService.copyScheduleDay(scheduleCopyDay);
        return Result.success();
    }

    /**
     * 手动放号
     */
    @GetMapping("/registration/deliver")
    @ApiOperation(value = "提前放号")
    public Result doctorDeliverRegistration(){
        cleanCache();
        doctorService.deliverRegistration();
        return Result.success();
    }

    /**
     * 查看某日每时段的挂号状态
     */
    @GetMapping("/registration/check")
    @ApiOperation("查看某日每时段的挂号状态")
    public Result<List<PatientAppointmentInfo>> doctorRegistrationCheck(@RequestParam String date){
        List<PatientAppointmentInfo> patients = doctorService.registrationCheck(date);
        return Result.success(patients);
    }

    /**
     * 设置患者是否失约
     */
    @PostMapping("/setPatientCredit")
    @ApiOperation("设置患者是否失约")
    public Result setPatientCredit(@RequestBody PatientAppointment_PatientInfoDTO patientAppointmentInfoDTO){
        doctorService.setPatientCredit(patientAppointmentInfoDTO);
        return Result.success();
    }

    /**
     * 查询患者历史预约信息
     */
    @GetMapping("/query/patient/appointment/{patientId}")
    @ApiOperation(value = "查询患者历史预约信息")
    public Result<List<AppointmentRecordsVO>> queryPatientAppointment(@PathVariable Long patientId){
        List<AppointmentRecordsVO> appointmentRecordsList = doctorService.queryPatientAppointment(patientId);
        return Result.success(appointmentRecordsList);
    }

    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    @ApiOperation(value = "医生修改密码")
    public Result doctorUpdatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO){
        doctorService.updatePassword(updatePasswordDTO);
        return Result.success();
    }

    private void cleanCache() {
        //清除所有缓存
        Set<String> keys = redisTemplate.keys("*doctor_scheduling_*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }
}
