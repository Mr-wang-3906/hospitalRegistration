package hospital.service.impl;

import hospital.constant.MessageConstant;
import hospital.context.BaseContext;
import hospital.dto.*;
import hospital.entity.*;
import hospital.exception.DeletionNotAllowedException;
import hospital.exception.PasswordErrorException;
import hospital.exception.RegisterFailedException;
import hospital.exception.UpdateFailedException;
import hospital.mapper.*;
import hospital.service.DoctorService;
import hospital.temp.DoctorInfo;
import hospital.temp.Doctor_SchedulingTemp;
import hospital.temp.PatientAppointmentInfo;
import hospital.utils.DataUtils;
import hospital.vo.Doctor_SchedulingVO;
import hospital.vo.ScheduleTemplateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;

import static hospital.controller.doctor.DoctorController.verCode;

@Service
public class DoctorServiceImpl implements DoctorService {


    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private PatientMapper patientMapper;

    /**
     * 新增医生
     **/
    @Transactional
    public void insertNewDoctor(DoctorRegisterDTO doctorRegisterDTO, HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        Map<String, String> codeMap = (Map<String, String>) session.getAttribute("verCode");
        String code;
        try {
            code = codeMap.get("code");
        } catch (Exception e) {
            //验证码过期，或未找到  ---验证码无效
            throw new RegisterFailedException(MessageConstant.REGISTER_TIMEOUT);
        }
        //验证码判断
        if (!verCode.toUpperCase().equals(code)) {
            throw new RegisterFailedException(MessageConstant.REGISTER_FAILED);
        }
        //验证码使用完后session删除
        session.removeAttribute("verCode");
        Doctor doctor = doctorMapper.selectByUsername(doctorRegisterDTO.getUserName());
        //用户名是否可用
        if (doctor != null) {
            //返回，该用户）（username）已被注册过
            throw new RegisterFailedException(MessageConstant.ACCOUNT_ALREADY_EXISTS);
        }
        //数据库插入数据
        Doctor doctorTemp = new Doctor();
        BeanUtils.copyProperties(doctorRegisterDTO, doctorTemp);
        doctorTemp.setPassword(DigestUtils.md5DigestAsHex(doctorTemp.getPassword().getBytes()));
        doctorMapper.insertNewDoctor(doctorTemp);
        //是否插入数据成功
        if (doctorMapper.selectByUsername(doctorTemp.getUserName()) == null) {
            //返回注册失败
            throw new RegisterFailedException(MessageConstant.REGISTER_FAILED_BUSY);
        }
        //注册成功
        //获取新注册的医生的id
        Doctor newDoctor = doctorMapper.selectByUsername(doctorTemp.getUserName());

        //在数据库医生排班为其开辟出90个空排班
        List<String> days = new LinkedList<>();
        List<String> lastMonth = DataUtils.getDayListOfMonth(-1);
        List<String> thisMonth = DataUtils.getDayListOfMonth(0);
        List<String> nextMonth = DataUtils.getDayListOfMonth(1);
        days.addAll(lastMonth);
        days.addAll(thisMonth);
        days.addAll(nextMonth);
        for (String day : days) {
            scheduleMapper.insertNewDoctorSchedule(newDoctor.getId(), DataUtils.parse(day, DataUtils.FORMAT_LONOGRAM));
        }
    }

    /**
     * 医生登录
     **/
    public Doctor login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        //根据用户名查询数据库中的数据
        Doctor doctor = doctorMapper.selectByUsername(username);

        //密码比对
        //对前端传过来的明文密码进行加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (doctor != null) {
            if (!password.equals(doctor.getPassword())) {
                //密码错误
                throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
            }
        }

        //返回实体对象
        return doctor;
    }

    /**
     * 新增挂号种类
     */
    public void addRegistrationType(RegistrationType registrationType) {
        registrationMapper.insertRegistrationType(BaseContext.getCurrentId(), registrationType);
    }

    /**
     * 查询挂号种类
     */
    public List<RegistrationType> queryRegistrationType() {
        return registrationMapper.queryRegistrationType(BaseContext.getCurrentId());
    }

    /**
     * 修改挂号种类
     */
    public void updateRegistrationType(RegistrationType registrationType) {
        registrationMapper.updateRegistrationType(BaseContext.getCurrentId(), registrationType);
    }

    /**
     * 删除挂号种类
     */
    @Transactional
    public void deleteRegistrationType(List<Long> ids) {
        for (Long id : ids) {
            //判断是否有模板正在使用该挂号类别或医生的排班
            List<ScheduleTemplate> scheduleTemplates = scheduleMapper.selectByRegistrationTypeId(id);
            List<Patient_Doctor_Scheduling> patientDoctorScheduling = scheduleMapper.selectPatientScheduleByRegistrationTypeId(id);
            List<Doctor_Scheduling> doctorSchedulingTemps = scheduleMapper.selectDoctorSchedulingTempByRegistrationTypeId(BaseContext.getCurrentId(), id);
            if (doctorSchedulingTemps.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_DOCTOR_SCHEDULE);
            }
            if (scheduleTemplates.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_TEMPLATE);
            } else if (patientDoctorScheduling.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_DOCTOR);
            }
            scheduleMapper.updateByRegistrationTypeId(id);
            registrationMapper.deleteRegistrationType(id);
        }
    }

    /**
     * 新增排班模板
     */
    @Transactional
    public void addTemplate(ScheduleTemplateDTO scheduleTemplateDTO) {
        scheduleTemplateDTO.setDoctorId(BaseContext.getCurrentId());
        StringBuilder registrationTypes_Ids = new StringBuilder();
        for (Long registrationTypeId : scheduleTemplateDTO.getRegistrationTypes_Ids()) {
            registrationTypes_Ids.append(registrationTypeId);
            registrationTypes_Ids.append(",");
        }
        registrationTypes_Ids.deleteCharAt(registrationTypes_Ids.length() - 1);
        scheduleMapper.insertTemplate(scheduleTemplateDTO, String.valueOf(registrationTypes_Ids));
    }


    /**
     * 查询排班模板
     */
    public List<ScheduleTemplateVO> queryTemplate(Long doctorId) {
        List<ScheduleTemplateVO> scheduleTemplateVOS = new LinkedList<>();
        List<ScheduleTemplate> scheduleTemplates = scheduleMapper.selectByDoctorId(doctorId);
        for (ScheduleTemplate scheduleTemplate : scheduleTemplates) {
            String[] RegistrationTypeIds = scheduleTemplate.getRegistrationTypeIds().split(",");
            List<RegistrationType> registrationTypes = new ArrayList<>();
            for (String registrationTypeId : RegistrationTypeIds) {
                registrationTypes.add(registrationMapper.selectById(Long.valueOf(registrationTypeId)));
            }
            ScheduleTemplateVO scheduleTemplateVO = ScheduleTemplateVO.builder()
                    .templateName(scheduleTemplate.getTemplateName())
                    .registrationTypes(registrationTypes)
                    .doctorId(BaseContext.getCurrentId())
                    .afternoonCheckNumber(scheduleTemplate.getAfternoonCheckNumber())
                    .morningCheckNumber(scheduleTemplate.getMorningCheckNumber())
                    .id(scheduleTemplate.getId())
                    .build();
            scheduleTemplateVOS.add(scheduleTemplateVO);


        }
        return scheduleTemplateVOS;
    }

    /**
     * 删除排班模板
     */
    @Transactional
    public void deleteTemplates(List<Long> ids) {
        //直接删除模板即可
        for (Long id : ids) {
            scheduleMapper.deleteByTemplateId(id);
        }
    }

    /**
     * 修改排班模板
     */
    @Transactional
    public void updateTemplate(ScheduleTemplateDTO scheduleTemplateDTO) {
        //直接修改
        scheduleTemplateDTO.setDoctorId(BaseContext.getCurrentId());
        StringBuilder registrationTypeIds = new StringBuilder();
        for (Long registrationTypeId : scheduleTemplateDTO.getRegistrationTypes_Ids()) {
            registrationTypeIds.append(registrationTypeId);
            registrationTypeIds.append(",");
        }
        registrationTypeIds.deleteCharAt(registrationTypeIds.length() - 1);
        scheduleMapper.updateTemplate(scheduleTemplateDTO, String.valueOf(registrationTypeIds));
    }

    /**
     * 查询医生信息
     */
    public DoctorInfo queryInfoById(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        DoctorInfo doctorInfo = new DoctorInfo();
        BeanUtils.copyProperties(doctor, doctorInfo);
        return doctorInfo;
    }

    /**
     * 修改医生信息
     */
    public void updateInfo(DoctorInfo doctorInfo) {
        doctorMapper.updateInfo(BaseContext.getCurrentId(), doctorInfo);
    }

    /**
     * 查询排班信息
     */
    @Transactional
    public List<Doctor_SchedulingVO> querySchedule(Long doctorId) {
        List<Doctor_Scheduling> doctorSchedulingList = scheduleMapper.selectDoctorScheduleByDoctorId(doctorId);
        List<Doctor_SchedulingVO> doctorSchedulingVOS = new LinkedList<>();
        for (Doctor_Scheduling doctorScheduling : doctorSchedulingList) {
            Doctor_SchedulingVO doctorSchedulingVO = new Doctor_SchedulingVO();
            String data = DataUtils.format(doctorScheduling.getData(), DataUtils.FORMAT_LONOGRAM);
            BeanUtils.copyProperties(doctorScheduling, doctorSchedulingVO);
            doctorSchedulingVO.setData(data);
            if (doctorScheduling.getRegistrationTypeIds() != null) {
                //再来处理registrationType连表
                String[] registrationTypeIds = doctorScheduling.getRegistrationTypeIds().split(",");
                List<RegistrationType> registrationTypes = new LinkedList<>();
                for (String registrationTypeId : registrationTypeIds) {
                    if (!registrationTypeId.equals("null")) {
                        if (registrationTypes != null) {
                            registrationTypes.add(registrationMapper.selectById(Long.valueOf(registrationTypeId)));
                        }
                    } else {
                        registrationTypes = null;
                    }
                }
                doctorSchedulingVO.setRegistrationTypes(registrationTypes);
                doctorSchedulingVOS.add(doctorSchedulingVO);
            } else {
                doctorSchedulingVOS.add(doctorSchedulingVO);
            }
        }

        return doctorSchedulingVOS;
    }

    /**
     * 使用模板为某天排班
     */
    public void setScheduleWithTemplate(DoctorSetScheduleWithTemplate doctorSetScheduleWithTemplate) {
        Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByIdAndDate(BaseContext.getCurrentId(), doctorSetScheduleWithTemplate.getDate());
        if (patientDoctorScheduling != null) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR);
        }
        ScheduleTemplate scheduleTemplate = scheduleMapper.selectById(doctorSetScheduleWithTemplate.getTemplateId());
        //新建一天的排班
        Doctor_Scheduling doctorScheduling = new Doctor_Scheduling();
        BeanUtils.copyProperties(scheduleTemplate, doctorScheduling);
        doctorScheduling.setId(null);
        doctorScheduling.setData(doctorSetScheduleWithTemplate.getDate());

        scheduleMapper.updateByDoctorIdAndDate(BaseContext.getCurrentId(), doctorSetScheduleWithTemplate.getDate(), doctorScheduling);
    }

    /**
     * 单独修改某天的排班
     */
    public void updateOneDaySchedule(Doctor_SchedulingTemp doctorSchedulingTemp) {
        doctorSchedulingTemp.setDoctorId(BaseContext.getCurrentId());
        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);
        for (String onlineDay : futureDaysList) {
            if (doctorSchedulingTemp.getData().equals(onlineDay)) {
                throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR);
            }
        }
        StringBuilder registrationTypeIds = new StringBuilder();
        if (doctorSchedulingTemp.getRegistrationType_Ids().size() > 0) {
            for (Long registrationTypeId : doctorSchedulingTemp.getRegistrationType_Ids()) {
                registrationTypeIds.append(registrationTypeId);
                registrationTypeIds.append(",");
            }
            registrationTypeIds.deleteCharAt(registrationTypeIds.length() - 1);
        } else {
            registrationTypeIds = null;
        }
        Doctor_Scheduling doctorScheduling = new Doctor_Scheduling();
        BeanUtils.copyProperties(doctorSchedulingTemp, doctorScheduling);
        doctorScheduling.setRegistrationTypeIds(String.valueOf(registrationTypeIds));
        doctorScheduling.setDoctorId(BaseContext.getCurrentId());
        String data = doctorSchedulingTemp.getData();

        doctorMapper.updateOneDaySchedule(doctorScheduling, data);
    }

//    /**
//     * 排班复制-周复制 (已废弃)
//     */
//    public void copyScheduleWeek_abandon(ScheduleCopy_week scheduleCopyWeek) {
//        String year = DataUtils.getYear(new Date());
//        //先取得复制源的排班日期
//        List<LocalDate> sourceDates = DataUtils.getWeekDates(Integer.parseInt(year), scheduleCopyWeek.getSourceMonth(), scheduleCopyWeek.getSourceWeekNumber());
//        //根据日期和医生id查找排班信息
//        List<Doctor_Scheduling> sourceSchedulingList = new LinkedList<>();
//        for (LocalDate sourceDate: sourceDates) {
//            Doctor_Scheduling doctorScheduling = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), String.valueOf(sourceDate));
//            sourceSchedulingList.add(doctorScheduling);
//        }
//
//        //再根据目标日期进行复制
//        List<LocalDate> targetDates = DataUtils.getWeekDates(Integer.parseInt(year), scheduleCopyWeek.getTargetMonth(), scheduleCopyWeek.getTargetWeekNumber());
//        for (int i = 0; i < sourceSchedulingList.size(); i++) {
//            scheduleMapper.copyOneDay(String.valueOf(targetDates.get(i)),sourceSchedulingList.get(i),BaseContext.getCurrentId());
//        }
//
//
//

    /**
     * 排班复制-周复制
     */
    public void copyScheduleWeek(ScheduleCopy_week weekNumber) {
        //取得复制源的天数
        int sourYear = Integer.parseInt(weekNumber.getSourWeekNumber().substring(0, 4));
        int sourWeekNum = Integer.parseInt(weekNumber.getSourWeekNumber().substring(7, 9));
        List<LocalDate> sourWeekDates = DataUtils.getWeekDates(sourYear, sourWeekNum);

        //取得目标的天数
        int targetYear = Integer.parseInt(weekNumber.getTargetWeekNumber().substring(0, 4));
        int targetWeekNum = Integer.parseInt(weekNumber.getTargetWeekNumber().substring(7, 9));
        List<LocalDate> targetWeekDates = DataUtils.getWeekDates(targetYear, targetWeekNum);
        //检查是否全放号了
        List<Patient_Doctor_Scheduling> patientDoctorSchedulings = new LinkedList<>();
        for (LocalDate targetDate : targetWeekDates) {
            patientDoctorSchedulings.add(scheduleMapper.selectPatientDoctorSchedulingByIdAndDate(BaseContext.getCurrentId(), java.sql.Date.valueOf(targetDate)));
        }
        if (patientDoctorSchedulings.stream()
                .filter(Objects::nonNull)
                .count() == 7) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR);
        }
        //取得复制源排班
        List<Doctor_Scheduling> sourceSchedulingList = new LinkedList<>();
        for (LocalDate sourDate : sourWeekDates) {
            Doctor_Scheduling schedule = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), String.valueOf(sourDate));
            sourceSchedulingList.add(schedule);
        }
        //更新操作
        for (int i = 0; i < sourceSchedulingList.size(); i++) {
            if (patientDoctorSchedulings.get(i) == null) {
                scheduleMapper.copyOneDay(String.valueOf(targetWeekDates.get(i)), sourceSchedulingList.get(i), BaseContext.getCurrentId());
            }
        }
    }

    /**
     * 排班复制-月复制
     */
    public void copyScheduleMonth(ScheduleCopy_month scheduleCopyMonth) {
        //获得算上今天一共七天的日期
        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);
        List<Patient_Doctor_Scheduling> patientDoctorSchedulings = new LinkedList<>();
        for (String onlineDate : futureDaysList) {
            patientDoctorSchedulings.add(scheduleMapper.selectPatientDoctorSchedulingByIdAndDate(BaseContext.getCurrentId(), java.sql.Date.valueOf(onlineDate)));
        }
        if (patientDoctorSchedulings.stream()
                .filter(Objects::nonNull)
                .count() == 7) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR);
        }

        int sourceMonth = Integer.parseInt(scheduleCopyMonth.getSourMonthNumber().substring(5, 7));
        int year = Integer.parseInt(DataUtils.getYear(new Date()));
        List<LocalDate> sourceDates = DataUtils.getMonthDates(year, sourceMonth);
        List<Doctor_Scheduling> sourceSchedulingList = new LinkedList<>();
        for (LocalDate sourceDate : sourceDates) {
            Doctor_Scheduling scheduling = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), String.valueOf(sourceDate));
            sourceSchedulingList.add(scheduling);
        }

        int targetMonth = Integer.parseInt(scheduleCopyMonth.getTargetMonthNumber().substring(5, 7));
        List<LocalDate> targetDates = DataUtils.getMonthDates(year, targetMonth);
        for (int i = 0; i < (Math.min(targetDates.size(), sourceSchedulingList.size())); i++) {
            if (targetDates.get(i).equals(LocalDate.now())) {
                for (int j = 0; j < 7; j++) {
                    if (patientDoctorSchedulings.get(j) == null) {
                        scheduleMapper.copyOneDay(String.valueOf(targetDates.get(i + j)), sourceSchedulingList.get(i + j), BaseContext.getCurrentId());
                    }
                }
            } else {
                scheduleMapper.copyOneDay(String.valueOf(targetDates.get(i)), sourceSchedulingList.get(i), BaseContext.getCurrentId());
            }
        }
    }

    /**
     * 排班复制-日复制
     */
    public void copyScheduleDay(ScheduleCopy_day scheduleCopyDay) {
        //检查是否已放号
        Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByIdAndDate(BaseContext.getCurrentId(), java.sql.Date.valueOf(scheduleCopyDay.getTargetDay()));
        if (patientDoctorScheduling != null) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR);
        }

        Doctor_Scheduling sourceSchedule = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), scheduleCopyDay.getSourDay());

        scheduleMapper.copyOneDay(scheduleCopyDay.getTargetDay(), sourceSchedule, BaseContext.getCurrentId());

    }

    /**
     * 提前放号
     */
    public void deliverRegistration() {
        //获得算上今天一共七天的日期
        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);


        for (String date : futureDaysList) {
            Doctor_Scheduling doctorScheduling = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), date);
            //再更新患者挂号界面
            scheduleMapper.updatePatientDoctorScheduling(doctorScheduling.getDoctorId(), doctorScheduling.getData(), doctorScheduling.getRegistrationTypeIds());

        }
    }

    /**
     * 查看某天每时段挂号状态
     */
    public List<PatientAppointmentInfo> registrationCheck(String date) {
        Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByDoctorIdAndDate(BaseContext.getCurrentId(), date);
        Doctor_Scheduling doctorScheduling = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), date);
        int morningNUm = doctorScheduling.getMorningCheckNumber() - patientDoctorScheduling.getRegistrationNumberMorning();
        int afternoonNUm = doctorScheduling.getAfternoonCheckNumber() - patientDoctorScheduling.getRegistrationNumberAfternoon();
        if (morningNUm + afternoonNUm == 0) {
            return null;
        } else {
            List<PatientAppointmentInfo> patients = new LinkedList<>();
            Doctor doctor = doctorMapper.selectById(BaseContext.getCurrentId());
            List<AppointmentRecords> appointmentRecordsList = appointmentMapper.selectByDoctorName(doctor.getName());
            for (AppointmentRecords appointmentRecord : appointmentRecordsList) {
                // 使用空格进行分割，获取日期部分
                String dateString = appointmentRecord.getRegistrationTime().split(" ")[0];
                if (dateString.equals(date)){
                    Long patientId = appointmentRecord.getPatientId();
                    Patient patient = patientMapper.selectById(patientId);
                    int noShowNumber = appointmentMapper.countNo_ShowNumber(patientId);
                    patient.setNoShowNumber(noShowNumber);
                    PatientAppointmentInfo patientAppointmentInfo = new PatientAppointmentInfo();
                    BeanUtils.copyProperties(patient, patientAppointmentInfo);
                    patientAppointmentInfo.setAppointmentNumber(appointmentRecord.getRegistrationTime());
                    patients.add(patientAppointmentInfo);
                }
            }
            return patients;
        }
    }

    /**
     * 设置患者是否失约
     */
    public void setPatientCredit(PatientAppointmentInfoDTO patientAppointmentInfoDTO) {
        Doctor doctor = doctorMapper.selectById(BaseContext.getCurrentId());
        if (patientAppointmentInfoDTO.getStatus().equals("已完成")){
            appointmentMapper.setStatusFinashed(doctor.getName(),patientAppointmentInfoDTO);
        }else {
            appointmentMapper.setStatusFinashed(doctor.getName(), patientAppointmentInfoDTO);
        }
    }


}
