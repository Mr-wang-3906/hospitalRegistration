package hospital.service.impl;

import hospital.constant.MessageConstant;
import hospital.context.BaseContext;
import hospital.dto.LoginDTO;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.entity.Patient_Doctor_Scheduling;
import hospital.entity.RegistrationType;
import hospital.exception.NetException;
import hospital.mapper.*;
import hospital.temp.Orders;
import hospital.temp.PatientInfo;
import hospital.exception.PasswordErrorException;
import hospital.exception.RegisterFailedException;
import hospital.service.PatientService;
import hospital.utils.DataUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static hospital.controller.doctor.DoctorController.verCode;

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
                throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
            }
        }

        //返回实体对象
        return patient;
    }

    /**
     * 患者注册
     */
    public void insertNewPatient(PatientRegisterDTO patientRegisterDTO, HttpServletRequest httpServletRequest) {
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
        Patient patient = patientMapper.selectByUsername(patientRegisterDTO.getUserName());
        //用户名是否可用
        if (patient != null) {
            //返回，该用户）（username）已被注册过
            throw new RegisterFailedException(MessageConstant.ACCOUNT_ALREADY_EXISTS);
        }
        //数据库插入数据
        Patient patientTemp = new Patient();
        BeanUtils.copyProperties(patientRegisterDTO, patientTemp);
        patientTemp.setPassword(DigestUtils.md5DigestAsHex(patientTemp.getPassword().getBytes()));
        patientMapper.insertPatient(patientTemp);
        //是否插入数据成功
        if (patientMapper.selectByUsername(patientTemp.getUserName()) == null) {
            //返回注册失败
            throw new RegisterFailedException(MessageConstant.REGISTER_FAILED_BUSY);
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
                doctors.add(doctorMapper.selectByIdAndSection(doctorId, patientCheckRegistrationDTO.getSection()));
            }
        } else {
            doctors.add(doctorMapper.selectByIdAndSection(null, patientCheckRegistrationDTO.getSection()));
        }
        return doctors;
    }

    /**
     * 选择医生
     */
    public List<Patient_Doctor_Scheduling> choiceDoctor(Long doctorId) {
        return scheduleMapper.selectPatientDoctorSchedulingByDoctorId(doctorId);
    }

    /**
     * 提交订单
     */
    public void choiceTime(Orders orders) {
        Long patientId = BaseContext.getCurrentId();
        /* 先设置订单状态,只有付款了才会更新挂号界面 */
        Doctor doctor = doctorMapper.selectById(orders.getDoctorId());
        //先设置历史订单-待支付
        appointmentMapper.setStatusOngoing(patientId, DataUtils.convertTimeFormat(orders.getChoiceTime()), doctor.getName(), "待支付");

        //设置定时任务:
        // 定义任务
        Runnable task = () -> {
            // 将待支付改为已取消
            appointmentMapper.updateStatus(patientId, DataUtils.convertTimeFormat(orders.getChoiceTime()), "已取消");
        };

        scheduledFuture = executorService.schedule(task, 15, TimeUnit.MINUTES);

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
        //先确认会不会有bug
        Patient_Doctor_Scheduling patientDoctorSchedulings = scheduleMapper.selectPatientDoctorSchedulingByIdAndDate(orders.getDoctorId(), orders.getDate());
        if (patientDoctorSchedulings.getRegistrationNumberMorning() == 0 || patientDoctorSchedulings.getRegistrationNumberAfternoon() == 0) {
            appointmentMapper.updateStatus(BaseContext.getCurrentId(), DataUtils.convertTimeFormat(orders.getChoiceTime()), "已终止");
            throw new NetException(MessageConstant.NET_ERROR);
        }
        //先取消之前的定时任务
        cancelTask();
        //再更新挂号界面
        int num1 = Integer.parseInt(orders.getChoiceTime().substring(0, 1));
        RegistrationType registrationTypes = registrationMapper.selectById(orders.getRegistrationTypeId());

        //然后更新历史订单
        appointmentMapper.updateStatus(BaseContext.getCurrentId(), DataUtils.convertTimeFormat(orders.getChoiceTime()), "已预约");
        if (num1 == 9) {
            scheduleMapper.updateConfirmPaymentNine(orders, registrationTypes.getEstimatedTime());
        } else {
            int num2 = Integer.parseInt(orders.getChoiceTime().substring(0, 2));
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


}
