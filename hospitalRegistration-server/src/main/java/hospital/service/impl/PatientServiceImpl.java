package hospital.service.impl;

import hospital.constant.MessageConstant;
import hospital.context.BaseContext;
import hospital.dto.LoginDTO;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.*;
import hospital.exception.AllException;
import hospital.mapper.*;
import hospital.temp.Orders;
import hospital.temp.PatientInfo;
import hospital.service.PatientService;
import hospital.utils.Code;
import hospital.vo.Patient_Doctor_SchedulingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用于取消定时任务
     **/
    // 获取服务器的全局线程池
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    static ScheduledFuture<?> scheduledFuture;


    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * 患者登录
     */
    public Patient login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        //根据用户名查询数据库中的数据
        Patient patient = patientMapper.selectByUsername(username);

        //密码比对
        //对前端传过来的明文密码进行加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (patient != null) {
            if (!password.equals(patient.getPassword())) {
                //密码错误
                throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.PASSWORD_ERROR);
            }
        }

        //返回实体对象
        return patient;
    }

    /**
     * 患者注册
     */
    public void insertNewPatient(PatientRegisterDTO patientRegisterDTO) {
        String redisKey = "email_" + patientRegisterDTO.getEmailAddress();

        // 检查Redis中是否存在指定的键
        boolean keyExists = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        String code;
        if (keyExists) {
            // 从Redis中获取验证码值
            code = redisTemplate.opsForValue().get(redisKey);
        } else {
            // Redis中不存在指定的键
            throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.REGISTER_TIMEOUT);
        }
        //验证码判断
        if (!patientRegisterDTO.getVerify().equals(code)) {
            throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.REGISTER_FAILED);
        }

        Patient patient = patientMapper.selectByUsername(patientRegisterDTO.getUserName());
        //用户名是否可用
        if (patient != null) {
            //返回，该用户）（username）已被注册过
            throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.ACCOUNT_ALREADY_EXISTS);
        }
        //数据库插入数据
        Patient patientTemp = new Patient();
        BeanUtils.copyProperties(patientRegisterDTO, patientTemp);
        patientTemp.setPassword(DigestUtils.md5DigestAsHex(patientTemp.getPassword().getBytes()));
        patientMapper.insertPatient(patientTemp);
        //是否插入数据成功
        if (patientMapper.selectByUsername(patientTemp.getUserName()) == null) {
            //返回注册失败
            throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.REGISTER_FAILED_BUSY);
        }
        //注册成功
    }

    /**
     * 查看患者信息
     */
    public PatientInfo queryInfo(Long patientId) {
        Patient patient = patientMapper.selectById(patientId);
        PatientInfo patientInfo = new PatientInfo();
        BeanUtils.copyProperties(patient, patientInfo);
        return patientInfo;
    }

    /**
     * 查看挂号界面
     */
    public List<Doctor> checkRegistration(PatientCheckRegistrationDTO patientCheckRegistrationDTO) {
        List<Patient_Doctor_Scheduling> patientDoctorSchedulings = scheduleMapper.selectPatientDoctorScheduling(patientCheckRegistrationDTO.getDate());
        List<Doctor> doctors = new LinkedList<>();
        if (patientDoctorSchedulings.size() > 0) {
            for (Patient_Doctor_Scheduling patientDoctorScheduling : patientDoctorSchedulings) {
                Long doctorId = patientDoctorScheduling.getDoctorId();
                doctors.addAll(doctorMapper.selectByIdAndSection(doctorId, patientCheckRegistrationDTO.getSection(), patientCheckRegistrationDTO.getDoctorName()));
            }
        } else {
            doctors.addAll(doctorMapper.selectByIdAndSection(null, patientCheckRegistrationDTO.getSection(), patientCheckRegistrationDTO.getDoctorName()));
        }
        return doctors;
    }

    /**
     * 选择医生
     */
    public List<Patient_Doctor_SchedulingVO> choiceDoctor(Long doctorId) {
        List<Patient_Doctor_Scheduling> patientDoctorSchedulings = scheduleMapper.selectPatientDoctorSchedulingByDoctorId(doctorId);
        List<Patient_Doctor_SchedulingVO> patientDoctorSchedulingVOs = new LinkedList<>();
        for (Patient_Doctor_Scheduling patientDoctorScheduling : patientDoctorSchedulings) {
            Patient_Doctor_SchedulingVO patientDoctorSchedulingVO = new Patient_Doctor_SchedulingVO();
            BeanUtils.copyProperties(patientDoctorScheduling, patientDoctorSchedulingVO);
            String registrationTypeId = patientDoctorScheduling.getRegistrationTypeId();
            String[] registrationTypeIds = registrationTypeId.split(",");
            List<RegistrationType> registrationTypes = new LinkedList<>();
            for (String typeId : registrationTypeIds) {
                RegistrationType registrationType = registrationMapper.selectById(Long.valueOf(typeId));
                registrationTypes.add(registrationType);
            }
            patientDoctorSchedulingVO.setRegistrationTypes(registrationTypes);
            patientDoctorSchedulingVOs.add(patientDoctorSchedulingVO);
        }
        return patientDoctorSchedulingVOs;
    }

    /**
     * 提交订单
     *
     * @return
     */
    public String choiceTime(Orders orders) {
        Long patientId = BaseContext.getCurrentId();
        /* 先设置订单状态,只有付款了才会更新挂号界面 */
        Doctor doctor = doctorMapper.selectById(orders.getDoctorId());
        //先设置历史订单-待支付
        setTime(orders);
        //生成单号
        String oddNumber = Code.setOddNumber();
        //生成该单剩余付款时间
        appointmentMapper.setStatusOngoing(patientId, orders, doctor.getName(), "unpaid", oddNumber);
        String lastTimeKey = "last_Time_" + oddNumber;
        redisTemplate.opsForValue().set(lastTimeKey, oddNumber + "单剩余时间", 15, TimeUnit.MINUTES);

        //设置定时任务:
        // 定义任务
        Runnable task = () -> {
            // 将待支付改为已取消
            appointmentMapper.updateStatus(patientId, orders.getChoiceTime(), "canceled", orders.getSection(), oddNumber);
        };

        scheduledFuture = executorService.schedule(task, 15, TimeUnit.MINUTES);

        return oddNumber;
    }

    private static void setTime(Orders orders) {
        int num1 = Integer.parseInt(orders.getChoiceTime().substring(0, 1));
        if (num1 == 9 || num1 == 0) {
            orders.setChoiceTime(orders.getDate() + " 9:00-10:00");
        } else {
            int num2 = Integer.parseInt(orders.getChoiceTime().substring(0, 2));
            switch (num2) {
                case 10:
                    orders.setChoiceTime(orders.getDate() + " 10:00-11:00");
                    break;
                case 11:
                    orders.setChoiceTime(orders.getDate() + " 11:00-12:00");
                    break;
                case 14:
                    orders.setChoiceTime(orders.getDate() + " 14:00-15:00");
                    break;
                case 15:
                    orders.setChoiceTime(orders.getDate() + " 15:00-16:00");
                    break;
                case 16:
                    orders.setChoiceTime(orders.getDate() + " 16:00-17:00");
                    break;
            }
        }
    }

    //设置定时任务取消函数,用于更新用户付款的更新
    public static void cancelTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }


    /**
     * 确认付款
     */
    public void confirmPayment(Orders orders) {
        String time = orders.getChoiceTime();
        setTime(orders);
        //先确认会不会有bug
        Patient_Doctor_Scheduling patientDoctorSchedulings = scheduleMapper.selectPatientDoctorSchedulingByIdAndDateAndRegistrationTypeIsNotNull(orders.getDoctorId(), orders.getDate());
        if (patientDoctorSchedulings.getRegistrationNumberMorning() == 0 || patientDoctorSchedulings.getRegistrationNumberAfternoon() == 0) {
            cancelTask();
            appointmentMapper.updateStatus(BaseContext.getCurrentId(), orders.getChoiceTime(), "stopped", orders.getSection(), orders.getOddNumber());
            throw new AllException(MessageConstant.Code_Internal_Server_Error,MessageConstant.NET_ERROR);
        }
        //先取消之前的定时任务
        cancelTask();
        //再更新挂号界面

        RegistrationType registrationTypes = registrationMapper.selectById(orders.getRegistrationTypeId());

        //然后更新历史订单
        appointmentMapper.updateStatus(BaseContext.getCurrentId(), orders.getChoiceTime(), "waiting", orders.getSection(), orders.getOddNumber());
        int num3 = Integer.parseInt(time.substring(0, 1));
        if (num3 == 9 || num3 == 0) {
            scheduleMapper.updateConfirmPaymentNine(orders, registrationTypes.getEstimatedTime());
        } else {
            int num2 = Integer.parseInt(time.substring(0, 2));
            switch (num2) {
                case 10:
                    scheduleMapper.updateConfirmPaymentTen(orders, registrationTypes.getEstimatedTime());
                    break;
                case 11:
                    scheduleMapper.updateConfirmPaymentEleven(orders, registrationTypes.getEstimatedTime());
                    break;
                case 14:
                    scheduleMapper.updateConfirmPaymentFourteen(orders, registrationTypes.getEstimatedTime());
                    break;
                case 15:
                    scheduleMapper.updateConfirmPaymentFifTeen(orders, registrationTypes.getEstimatedTime());
                    break;
                case 16:
                    scheduleMapper.updateConfirmPaymentSixTeen(orders, registrationTypes.getEstimatedTime());
                    break;
            }
        }
    }

    /**
     * 取消预约
     */
    public void cancelPayment(Orders orders) {
        cancelTask();
        setTime(orders);
        appointmentMapper.updateStatus(BaseContext.getCurrentId(), orders.getChoiceTime(), "canceled", orders.getSection(), orders.getOddNumber());

    }

}
