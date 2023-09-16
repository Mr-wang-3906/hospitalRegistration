package hospital.controller.task;

import hospital.entity.Doctor;
import hospital.entity.Doctor_Scheduling;
import hospital.mapper.DoctorMapper;
import hospital.mapper.ScheduleMapper;
import hospital.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 定时任务类
 * 用于完成医生排班的日、月更新以及患者挂号界面的日更新
 */
@Component
public class Tasks {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * 每天12点触发 更新患者挂号界面
     **/
    @Scheduled(cron = "0 0 0 * * ?")
    public void executeTask1() {
        //获得算上今天一共七天的日期
        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);

        //把患者挂号界面所有日期全部加一
        scheduleMapper.updatePatientDoctorSchedulingDate();

        //获取所有在职医生的id
        List<Doctor> doctors = doctorMapper.selectAll();
        //对每一位医生: 根据日期从医生日常排班里取
        for (Doctor doctor : doctors) {
            for (String date : futureDaysList) {
                Doctor_Scheduling doctorScheduling = scheduleMapper.selectByDoctorIdAndDate(doctor.getId(), date);
                //再更新患者挂号界面
                scheduleMapper.updatePatientDoctorScheduling(doctorScheduling.getDoctorId(), doctorScheduling.getData(), doctorScheduling.getRegistrationTypeIds());
            }
        }
    }

    /**
     * 每月1号零点触发,更新日常排班
     **/
    @Scheduled(cron = "0 0 0 1 * ?")
    public void executeTask2() {
        cleanCache();
        //先获得前一个月的月份
        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);
        //删除前一个月的数据
        scheduleMapper.deleteDoctorSchedulingMonth(previousMonth.getMonthValue());

        //获取后一个月的日期
        List<LocalDate> nextDates = DataUtils.getMonthDates(LocalDateTime.now().getYear(), previousMonth.getMonthValue() + 2);
        //获取所有在职医生的id
        List<Doctor> doctors = doctorMapper.selectAll();
        for (Doctor doctor : doctors) {
            //在数据库新增它们的位置
            for (LocalDate nextDate : nextDates) {
                scheduleMapper.insertNextMonthSchedules(doctor.getId(),String.valueOf(nextDate));
            }
        }

    }

    private void cleanCache() {
        //清除所有缓存
        Set keys = redisTemplate.keys("*doctor_scheduling_*");
        redisTemplate.delete(keys);
    }
}