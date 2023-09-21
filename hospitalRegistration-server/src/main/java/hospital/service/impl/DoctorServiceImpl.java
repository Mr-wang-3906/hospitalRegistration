package hospital.service.impl;

import hospital.constant.MessageConstant;
import hospital.context.BaseContext;
import hospital.dto.*;
import hospital.entity.*;
import hospital.exception.*;
import hospital.mapper.*;
import hospital.service.DoctorService;
import hospital.temp.DoctorInfo;
import hospital.temp.Doctor_SchedulingTemp;
import hospital.temp.PatientAppointmentInfo;
import hospital.utils.DataUtils;
import hospital.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 新增医生
     **/
    @Transactional
    public void insertNewDoctor(DoctorRegisterDTO doctorRegisterDTO) {
        String redisKey = "email_" + doctorRegisterDTO.getEmailAddress();

        // 检查Redis中是否存在指定的键
        boolean keyExists = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        String code;
        if (keyExists) {
            // 从Redis中获取验证码值
            code = redisTemplate.opsForValue().get(redisKey);
        } else {
            // Redis中不存在指定的键
            throw new RegisterFailedException(MessageConstant.REGISTER_TIMEOUT);
        }
        //验证码判断
        if (!doctorRegisterDTO.getVerify().equals(code)) {
            throw new RegisterFailedException(MessageConstant.REGISTER_FAILED);
        }

        Doctor doctor = doctorMapper.selectByUsername(doctorRegisterDTO.getUsername());
        //用户名是否可用
        if (doctor != null) {
            //返回，该用户）（username）已被注册过
            throw new RegisterFailedException(MessageConstant.ACCOUNT_ALREADY_EXISTS);
        }
        //数据库插入数据
        Doctor doctorTemp = new Doctor();
        BeanUtils.copyProperties(doctorRegisterDTO, doctorTemp);
        doctorTemp.setUserName(doctorRegisterDTO.getUsername());
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

        //为新医生开辟七个预约挂号日期
        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);
        for (String day : futureDaysList) {
            scheduleMapper.insertNewDoctorAppointment(newDoctor.getId(), day);
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
    @Transactional
    public void addRegistrationType(RegistrationType registrationType) {
        RegistrationType registrationType1 = registrationMapper.selectByName(registrationType.getRegistrationName());
        if (registrationType1 != null) {
            throw new DateException(MessageConstant.INSERT_ERROR_EXISTS);
        }
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
        List<RegistrationType> registrationTypes = registrationMapper.selecetByDoctorId(BaseContext.getCurrentId());
        if (registrationTypes.size() == ids.size()) {
            throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_DOCTOR_REGISTRATION);
        }
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
     *
     * @return
     */
    @Transactional
    public Long addTemplate(ScheduleTemplateDTO scheduleTemplateDTO) {
        //排查是有已经存在
        ScheduleTemplate scheduleTemplate = scheduleMapper.selectByName(scheduleTemplateDTO.getTemplateName());
        if (scheduleTemplate != null) {
            throw new DateException(MessageConstant.INSERT_ERROR_EXISTS);
        }

        scheduleTemplateDTO.setDoctorId(BaseContext.getCurrentId());
        StringBuilder registrationTypes_Ids = new StringBuilder();
        for (RegistrationType registrationType : scheduleTemplateDTO.getRegistrationTypes()) {
            registrationTypes_Ids.append(registrationType.getId());
            registrationTypes_Ids.append(",");
        }
        registrationTypes_Ids.deleteCharAt(registrationTypes_Ids.length() - 1);
        Map<String, Object> paramMap = new HashMap<>();// 模板类对象
        paramMap.put("template", scheduleTemplateDTO);
        paramMap.put("registrationTypeIds", registrationTypes_Ids.toString()); // 字符串参数
        scheduleMapper.insertTemplate(paramMap);
        return (Long) (paramMap.get("id"));
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
        for (RegistrationType registrationType : scheduleTemplateDTO.getRegistrationTypes()) {
            registrationTypeIds.append(registrationType.getId());
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
    @Transactional
    public void updateInfo(DoctorInfo doctorInfo) {
        if (doctorInfo.getName() == null || doctorInfo.getName().equals("")) {
            throw new DateException(MessageConstant.UPDATE_DOCTOR_ERROR_NULL);
        }

        doctorMapper.updateInfo(BaseContext.getCurrentId(), doctorInfo);
    }

    /**
     * 查询排班信息
     */
    @Transactional
    public ArrayList<Doctor_Scheduling_includingStatusVO> querySchedule(Long doctorId) {
        List<Doctor_Scheduling_includingStatus> doctorSchedulingList = scheduleMapper.selectDoctorScheduleByDoctorId(doctorId);
        ArrayList<Doctor_Scheduling_includingStatusVO> doctorSchedulingVOS = new ArrayList<>();
        for (Doctor_Scheduling_includingStatus doctorScheduling : doctorSchedulingList) {
            Doctor_Scheduling_includingStatusVO doctorSchedulingIncludingStatusVO = new Doctor_Scheduling_includingStatusVO();
            String data = DataUtils.format(doctorScheduling.getData(), DataUtils.FORMAT_LONOGRAM);
            BeanUtils.copyProperties(doctorScheduling, doctorSchedulingIncludingStatusVO);
            doctorSchedulingIncludingStatusVO.setDate(data);
            if (doctorScheduling.getRegistrationTypeIds() != null) {
                //再来处理registrationType连表
                String[] registrationTypeIds = doctorScheduling.getRegistrationTypeIds().split(",");
                ArrayList<RegistrationType> registrationTypes = new ArrayList<>();
                for (String registrationTypeId : registrationTypeIds) {
                    if (!registrationTypeId.equals("null")) {
                        if (registrationMapper.selectById(Long.valueOf(registrationTypeId)) != null) {
                            registrationTypes.add(registrationMapper.selectById(Long.valueOf(registrationTypeId)));
                        }
                    }
                }
                doctorSchedulingIncludingStatusVO.setRegistrationTypes(registrationTypes);
                doctorSchedulingVOS.add(doctorSchedulingIncludingStatusVO);
            } else {
                doctorSchedulingIncludingStatusVO.setRegistrationTypes(new ArrayList<>());
                doctorSchedulingVOS.add(doctorSchedulingIncludingStatusVO);
            }
        }

        return doctorSchedulingVOS;
    }

    /**
     * 使用模板为某天排班
     */
    @Transactional
    public void setScheduleWithTemplate(DoctorSetScheduleWithTemplate doctorSetScheduleWithTemplate) {
        // 将 java.sql.Date 转换为 java.time.LocalDate
        LocalDate localDate = doctorSetScheduleWithTemplate.getDate().toLocalDate();
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 比较日期
        if (localDate.isBefore(today)) {
            throw new DateException(MessageConstant.DATE_SET_ERROR);
        }
        Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByIdAndDateAndRegistrationTypeIsNotNull(BaseContext.getCurrentId(), String.valueOf(doctorSetScheduleWithTemplate.getDate()));
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
        // 将 java.sql.Date 转换为 java.time.LocalDate
        LocalDate localDate = LocalDate.parse(doctorSchedulingTemp.getData());
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 比较日期
        if (localDate.isBefore(today)) {
            throw new DateException(MessageConstant.DATE_SET_ERROR);
        }
        doctorSchedulingTemp.setDoctorId(BaseContext.getCurrentId());
        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);
        for (String date : futureDaysList) {
            if (Objects.equals(date, doctorSchedulingTemp.getData())) {
                Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByDoctorIdAndDate(BaseContext.getCurrentId(), doctorSchedulingTemp.getData());
                if (!(patientDoctorScheduling.getRegistrationTypeId() == null)) {
                    throw new DateException(MessageConstant.DELETE_FAILED_DOCTOR);
                }
            }
        }
        StringBuilder registrationTypeIds = new StringBuilder();
        if (doctorSchedulingTemp.getRegistrationTypes().size() > 0) {
            for (RegistrationType registrationType : doctorSchedulingTemp.getRegistrationTypes()) {
                registrationTypeIds.append(registrationType.getId());
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
    @Transactional
    public void copyScheduleWeek(ScheduleCopy_week weekNumber) {
        //取得复制源的天数
        int sourYear = Integer.parseInt(weekNumber.getSourWeekNumber().substring(0, 4));
        int sourWeekNum = Integer.parseInt(weekNumber.getSourWeekNumber().substring(7, 9));
        List<LocalDate> sourWeekDates = DataUtils.getWeekDates(sourYear, sourWeekNum);

        //取得目标的天数
        int targetYear = Integer.parseInt(weekNumber.getTargetWeekNumber().substring(0, 4));
        int targetWeekNum = Integer.parseInt(weekNumber.getTargetWeekNumber().substring(7, 9));
        List<LocalDate> targetWeekDates = DataUtils.getWeekDates(targetYear, targetWeekNum);
        //检查是否有早于今天的
        for (LocalDate targetDate : targetWeekDates) {
            // 获取当前日期
            LocalDate today = LocalDate.now();
            // 比较日期
            if (targetDate.isBefore(today)) {
                throw new DateException(MessageConstant.DATE_SET_ERROR);
            }
        }

        //检查是否全放号了
        List<Patient_Doctor_Scheduling> patientDoctorSchedulings = new LinkedList<>();
        for (LocalDate targetDate : targetWeekDates) {
            patientDoctorSchedulings.add(scheduleMapper.selectPatientDoctorSchedulingByIdAndDateAndRegistrationTypeIsNotNull(BaseContext.getCurrentId(), String.valueOf(targetDate)));
        }
        //查出来不为空就是已放号
        //若全不为空,则全已放号
        if (patientDoctorSchedulings.stream().allMatch(Objects::nonNull)) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR_ALL);
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
        //得到目标月
        String targetMonth = (scheduleCopyMonth.getTargetMonthNumber().substring(5, 7));
        //将那七天替换为目标月的日期
        futureDaysList = DataUtils.replaceMonth(futureDaysList, targetMonth);
        List<Patient_Doctor_Scheduling> patientDoctorSchedulings = new LinkedList<>();
        //查看目标天是否全放号(不为空则已放号,存在一个空则还有未放号的)
        for (String onlineDate : futureDaysList) {
            patientDoctorSchedulings.add(scheduleMapper.selectPatientDoctorSchedulingByIdAndDateAndRegistrationTypeIsNotNull(BaseContext.getCurrentId(), onlineDate));
        }
        //若全不为空,表明已全放号
        if (patientDoctorSchedulings.stream().allMatch(Objects::nonNull)) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR_ALL);
        }

        String sourceMonth = (scheduleCopyMonth.getSourMonthNumber().substring(5, 7));
        int year = Integer.parseInt(DataUtils.getYear(new Date()));
        List<LocalDate> sourceDates = DataUtils.getMonthDates(year, Integer.parseInt(sourceMonth));
        List<Doctor_Scheduling> sourceSchedulingList = new LinkedList<>();
        for (LocalDate sourceDate : sourceDates) {
            Doctor_Scheduling scheduling = scheduleMapper.selectByDoctorIdAndDate(BaseContext.getCurrentId(), String.valueOf(sourceDate));
            sourceSchedulingList.add(scheduling);
        }

        List<LocalDate> targetDates = DataUtils.getMonthDates(year, Integer.parseInt(targetMonth));
        //检查是否早于今天
        for (LocalDate target : targetDates) {
            // 获取当前日期
            LocalDate today = LocalDate.now();
            // 比较日期
            if (target.isBefore(today)) {
                throw new DateException(MessageConstant.DATE_SET_ERROR);
            }
        }

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
        Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByIdAndDateAndRegistrationTypeIsNotNull(BaseContext.getCurrentId(), scheduleCopyDay.getTargetDay());
        if (patientDoctorScheduling != null) {
            throw new UpdateFailedException(MessageConstant.DELETE_FAILED_DOCTOR);
        }
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 比较日期
        if (LocalDate.parse(scheduleCopyDay.getTargetDay()).isBefore(today)) {
            throw new DateException(MessageConstant.DATE_SET_ERROR);
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
            //查出来的表明已放号
            Patient_Doctor_Scheduling patientDoctorScheduling = scheduleMapper.selectPatientDoctorSchedulingByIdAndDateAndRegistrationTypeIsNotNull(BaseContext.getCurrentId(), date);
            if (patientDoctorScheduling == null && doctorScheduling.getRegistrationTypeIds() != null) {
                //先更新排班界面
                scheduleMapper.updateDeliverRegistration(BaseContext.getCurrentId(), date);
                //再更新患者挂号界面
                scheduleMapper.updatePatientDoctorScheduling2(doctorScheduling);
            }

        }
    }

    /**
     * 查看某天六个时段的挂号状态
     */
    public List<PatientAppointmentInfo> registrationCheck(String date) {
        Doctor doctor = doctorMapper.selectById(BaseContext.getCurrentId());
        List<PatientAppointmentInfo> patients = new LinkedList<>();
        PatientAppointmentInfo patients_nine_ten = new PatientAppointmentInfo();
        PatientAppointmentInfo patients_ten_eleven = new PatientAppointmentInfo();
        PatientAppointmentInfo patients_eleven_twelve = new PatientAppointmentInfo();
        PatientAppointmentInfo patients_fourteen_fifteen = new PatientAppointmentInfo();
        PatientAppointmentInfo patients_fifteen_sixteen = new PatientAppointmentInfo();
        PatientAppointmentInfo patients_sixteen_seventeen = new PatientAppointmentInfo();
        String nine_ten = date + " 9:00-10:00";
        String ten_eleven = date + " 10:00-11:00";
        String eleven_twelve = date + " 11:00-12:00";
        String fourteen_fifteen = date + " 14:00-15:00";
        String fifteen_sixteen = date + " 15:00-16:00";
        String sixteen_seventeen = date + " 16:00-17:00";
        List<AppointmentRecords> appointmentRecordsList = appointmentMapper.selectByDoctorName(doctor.getName());
        List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos_n_t = new LinkedList<>();
        List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos_t_e = new LinkedList<>();
        List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos_e_t = new LinkedList<>();
        List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos_f_f = new LinkedList<>();
        List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos_f_s = new LinkedList<>();
        List<PatientAppiontmentPationInfo> patientAppiontmentPationInfos_s_s = new LinkedList<>();
        patients_nine_ten.setAppointmentNumber(nine_ten);
        patients_ten_eleven.setAppointmentNumber(ten_eleven);
        patients_eleven_twelve.setAppointmentNumber(eleven_twelve);
        patients_fourteen_fifteen.setAppointmentNumber(fourteen_fifteen);
        patients_fifteen_sixteen.setAppointmentNumber(fifteen_sixteen);
        patients_sixteen_seventeen.setAppointmentNumber(sixteen_seventeen);
        for (AppointmentRecords appointmentRecord : appointmentRecordsList) {
            if (appointmentRecord.getRegistrationTime().equals(nine_ten)) {
                if (!Objects.equals(appointmentRecord.getRegistrationStatus(), "canceled") && !Objects.equals(appointmentRecord.getRegistrationStatus(), "stopped")) {
                    PatientAppiontmentPationInfo pationInfo = new PatientAppiontmentPationInfo();
                    pationInfo.setAppointmentStatus(appointmentRecord.getRegistrationStatus());
                    Patient patient = patientMapper.selectById(appointmentRecord.getPatientId());
                    BeanUtils.copyProperties(patient, pationInfo);
                    pationInfo.setPatientId(patient.getId());
                    pationInfo.setAppointmentId(appointmentRecord.getId());
                    pationInfo.setRegistrationName(registrationMapper.selectById(appointmentRecord.getRegistrationTypeId()).getRegistrationName());
                    pationInfo.setNoShowNumber(patient.getNoShowNumber());
                    patientAppiontmentPationInfos_n_t.add(pationInfo);
                }
            } else if (appointmentRecord.getRegistrationTime().equals(ten_eleven)) {
                if (!Objects.equals(appointmentRecord.getRegistrationStatus(), "canceled") && !Objects.equals(appointmentRecord.getRegistrationStatus(), "stopped")) {
                    PatientAppiontmentPationInfo pationInfo = new PatientAppiontmentPationInfo();
                    pationInfo.setAppointmentStatus(appointmentRecord.getRegistrationStatus());
                    Patient patient = patientMapper.selectById(appointmentRecord.getPatientId());
                    BeanUtils.copyProperties(patient, pationInfo);
                    pationInfo.setPatientId(patient.getId());
                    pationInfo.setAppointmentId(appointmentRecord.getId());
                    pationInfo.setRegistrationName(registrationMapper.selectById(appointmentRecord.getRegistrationTypeId()).getRegistrationName());
                    pationInfo.setNoShowNumber(patient.getNoShowNumber());
                    patientAppiontmentPationInfos_t_e.add(pationInfo);
                }
            } else if (appointmentRecord.getRegistrationTime().equals(eleven_twelve)) {
                if (!Objects.equals(appointmentRecord.getRegistrationStatus(), "canceled") && !Objects.equals(appointmentRecord.getRegistrationStatus(), "stopped")) {
                    PatientAppiontmentPationInfo pationInfo = new PatientAppiontmentPationInfo();
                    pationInfo.setAppointmentStatus(appointmentRecord.getRegistrationStatus());
                    Patient patient = patientMapper.selectById(appointmentRecord.getPatientId());
                    BeanUtils.copyProperties(patient, pationInfo);
                    pationInfo.setPatientId(patient.getId());
                    pationInfo.setAppointmentId(appointmentRecord.getId());
                    pationInfo.setRegistrationName(registrationMapper.selectById(appointmentRecord.getRegistrationTypeId()).getRegistrationName());
                    pationInfo.setNoShowNumber(patient.getNoShowNumber());
                    patientAppiontmentPationInfos_e_t.add(pationInfo);
                }
            } else if (appointmentRecord.getRegistrationTime().equals(fourteen_fifteen)) {
                if (!Objects.equals(appointmentRecord.getRegistrationStatus(), "canceled") && !Objects.equals(appointmentRecord.getRegistrationStatus(), "stopped")) {
                    PatientAppiontmentPationInfo pationInfo = new PatientAppiontmentPationInfo();
                    pationInfo.setAppointmentStatus(appointmentRecord.getRegistrationStatus());
                    Patient patient = patientMapper.selectById(appointmentRecord.getPatientId());
                    BeanUtils.copyProperties(patient, pationInfo);
                    pationInfo.setPatientId(patient.getId());
                    pationInfo.setAppointmentId(appointmentRecord.getId());
                    pationInfo.setRegistrationName(registrationMapper.selectById(appointmentRecord.getRegistrationTypeId()).getRegistrationName());
                    pationInfo.setNoShowNumber(patient.getNoShowNumber());
                    patientAppiontmentPationInfos_f_f.add(pationInfo);
                }
            } else if (appointmentRecord.getRegistrationTime().equals(fifteen_sixteen)) {
                if (!Objects.equals(appointmentRecord.getRegistrationStatus(), "canceled") && !Objects.equals(appointmentRecord.getRegistrationStatus(), "stopped")) {
                    PatientAppiontmentPationInfo pationInfo = new PatientAppiontmentPationInfo();
                    pationInfo.setAppointmentStatus(appointmentRecord.getRegistrationStatus());
                    Patient patient = patientMapper.selectById(appointmentRecord.getPatientId());
                    BeanUtils.copyProperties(patient, pationInfo);
                    pationInfo.setPatientId(patient.getId());
                    pationInfo.setAppointmentId(appointmentRecord.getId());
                    pationInfo.setRegistrationName(registrationMapper.selectById(appointmentRecord.getRegistrationTypeId()).getRegistrationName());
                    pationInfo.setNoShowNumber(patient.getNoShowNumber());
                    patientAppiontmentPationInfos_f_s.add(pationInfo);
                }
            } else if (appointmentRecord.getRegistrationTime().equals(sixteen_seventeen)) {
                if (!Objects.equals(appointmentRecord.getRegistrationStatus(), "canceled") && !Objects.equals(appointmentRecord.getRegistrationStatus(), "stopped")) {
                    PatientAppiontmentPationInfo pationInfo = new PatientAppiontmentPationInfo();
                    pationInfo.setAppointmentStatus(appointmentRecord.getRegistrationStatus());
                    Patient patient = patientMapper.selectById(appointmentRecord.getPatientId());
                    BeanUtils.copyProperties(patient, pationInfo);
                    pationInfo.setPatientId(patient.getId());
                    pationInfo.setAppointmentId(appointmentRecord.getId());
                    pationInfo.setRegistrationName(registrationMapper.selectById(appointmentRecord.getRegistrationTypeId()).getRegistrationName());
                    pationInfo.setNoShowNumber(patient.getNoShowNumber());
                    patientAppiontmentPationInfos_s_s.add(pationInfo);
                }
            }

        }
        patients_nine_ten.setPatientAppiontmentPationInfos(patientAppiontmentPationInfos_n_t);
        patients_ten_eleven.setPatientAppiontmentPationInfos(patientAppiontmentPationInfos_t_e);
        patients_eleven_twelve.setPatientAppiontmentPationInfos(patientAppiontmentPationInfos_e_t);
        patients_fourteen_fifteen.setPatientAppiontmentPationInfos(patientAppiontmentPationInfos_f_f);
        patients_fifteen_sixteen.setPatientAppiontmentPationInfos(patientAppiontmentPationInfos_f_s);
        patients_sixteen_seventeen.setPatientAppiontmentPationInfos(patientAppiontmentPationInfos_s_s);
        patients.add(patients_nine_ten);
        patients.add(patients_ten_eleven);
        patients.add(patients_eleven_twelve);
        patients.add(patients_fourteen_fifteen);
        patients.add(patients_fifteen_sixteen);
        patients.add(patients_sixteen_seventeen);
        return patients;
    }


    /**
     * 设置患者是否失约
     */
    @Transactional
    public void setPatientCredit(PatientAppointment_PatientInfoDTO patientAppointmentInfoDTO) {
        if (Objects.equals(patientAppointmentInfoDTO.getStatus(), "noVisited")) {
            patientMapper.addPatientNo_Show_Number(patientAppointmentInfoDTO.getPatientId());
        }
        patientMapper.updatePatient(patientAppointmentInfoDTO);
        appointmentMapper.setStatusFinashed(patientAppointmentInfoDTO);
    }

    /**
     * 查看患者历史预约信息
     */
    public List<AppointmentRecordsVO> queryPatientAppointment(Long patientId) {

        List<AppointmentRecords> records = appointmentMapper.selectByPatientId(patientId);
        List<AppointmentRecordsVO> appointmentRecordsVOS = new LinkedList<>();
        for (AppointmentRecords record : records) {
            List<RegistrationType> registrationTypes = new LinkedList<>();
            RegistrationType registrationType = registrationMapper.selectById(record.getRegistrationTypeId());
            registrationTypes.add(registrationType);
            AppointmentRecordsVO appointmentRecordsVO = new AppointmentRecordsVO();
            BeanUtils.copyProperties(record, appointmentRecordsVO);
            appointmentRecordsVO.setRegistrationTypes(registrationTypes);
            //处于未支付状态的订单获取剩余时间
            if (record.getRegistrationStatus().equals("unpaid")) {
                String lastTimeKey = "last_Time_" + record.getOddNumber();
                // 获取剩余时间（以毫秒为单位）
                Long Time = redisTemplate.getExpire(lastTimeKey, TimeUnit.SECONDS);
                appointmentRecordsVO.setLastTime(Time);
            }
            appointmentRecordsVOS.add(appointmentRecordsVO);
        }
        return appointmentRecordsVOS;
    }

    /**
     * 修改密码
     */
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        Doctor doctor = doctorMapper.selectById(BaseContext.getCurrentId());
        if ((doctor.getPassword().equals(DigestUtils.md5DigestAsHex(updatePasswordDTO.getOld_password().getBytes())))) {
            doctorMapper.updatePassword(BaseContext.getCurrentId(), updatePasswordDTO.getNew_password());
        } else {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
    }


}
