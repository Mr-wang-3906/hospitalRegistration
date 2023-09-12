package hospital.service.impl;

import hospital.constant.MessageConstant;
import hospital.dto.LoginDTO;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.entity.Patient_Doctor_Scheduling;
import hospital.entity.RegistrationType;
import hospital.mapper.DoctorMapper;
import hospital.mapper.RegistrationMapper;
import hospital.mapper.ScheduleMapper;
import hospital.temp.PatientInfo;
import hospital.exception.PasswordErrorException;
import hospital.exception.RegisterFailedException;
import hospital.mapper.PatientMapper;
import hospital.service.PatientService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
                doctors.add(doctorMapper.selectByIdAndSection(doctorId,patientCheckRegistrationDTO.getSection()));
            }
        }else {
            doctors.add(doctorMapper.selectByIdAndSection(null,patientCheckRegistrationDTO.getSection()));
        }
        return doctors;
    }

    /**
     * 选择医生
     */
    public List<RegistrationType> choiceDoctor(Long doctorId) {
        return registrationMapper.queryRegistrationType(doctorId);
    }
}
