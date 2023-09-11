package hospital.service.impl;

import hospital.constant.MessageConstant;
import hospital.context.BaseContext;
import hospital.dto.LoginDTO;
import hospital.dto.DoctorRegisterDTO;
import hospital.dto.ScheduleTemplateDTO;
import hospital.entity.*;
import hospital.exception.DeletionNotAllowedException;
import hospital.exception.PasswordErrorException;
import hospital.exception.RegisterFailedException;
import hospital.mapper.DoctorMapper;
import hospital.mapper.RegistrationMapper;
import hospital.mapper.ScheduleMapper;
import hospital.service.DoctorService;
import hospital.temp.DoctorInfo;
import hospital.temp.Doctor_Scheduling_Temp;
import hospital.utils.DataUtils;
import hospital.vo.Doctor_SchedulingVO;
import hospital.vo.ScheduleTemplateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static hospital.controller.doctor.DoctorController.verCode;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

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
    public void deleteRegistrationType(List<Long> ids) {
        for (Long id : ids) {
            //判断是否有模板正在使用该挂号类别或医生的排班
            List<ScheduleTemplate> scheduleTemplates = scheduleMapper.selectByRegistrationTypeId(id);
            List<Patient_Doctor_Scheduling> patientDoctorScheduling = scheduleMapper.selectPatientScheduleByRegistrationTypeId(id);
            List<Doctor_Scheduling_Temp> doctorSchedulingTemps = scheduleMapper.selectDoctorSchedulingTempByRegistrationTypeId(BaseContext.getCurrentId(), id);
            for (Doctor_Scheduling_Temp temp : doctorSchedulingTemps) {
                if (temp.getOneRegistrationTypeNumber() == 1){
                    throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_DOCTOR_SCHEDULE);
                }
            }
            if (scheduleTemplates.size() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_TEMPLATE);
            } else if (patientDoctorScheduling.size() > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DELETE_FAILED_DOCTOR);
            }
            scheduleMapper.deleteByRegistrationTypeId(id);
            registrationMapper.deleteRegistrationType(id);
        }
    }

    /**
     * 新增排班模板
     */
    @Transactional
    public void addTemplate(ScheduleTemplateDTO scheduleTemplateDTO) {
        scheduleTemplateDTO.setDoctorId(BaseContext.getCurrentId());
        for (Long registrationTypeId : scheduleTemplateDTO.getRegistrationTypes_Ids()) {
            scheduleMapper.insertTemplate(scheduleTemplateDTO, registrationTypeId);
        }
    }


    /**
     * 查询排班模板
     */
    public List<ScheduleTemplateVO> queryTemplate(Long doctorId) {
        List<ScheduleTemplateVO> scheduleTemplateVOS = new LinkedList<>();
        List<ScheduleTemplate> scheduleTemplates = scheduleMapper.selectByDoctorId(doctorId);
        for (ScheduleTemplate scheduleTemplate : scheduleTemplates) {
            List<RegistrationType> registrationTypes = registrationMapper.selectById(scheduleTemplate.getRegistrationTypeId());
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
        //先全把原来的全删了
        scheduleMapper.deleteByTemplateId(scheduleTemplateDTO.getId());
        //再把新的给加上去
        scheduleTemplateDTO.setDoctorId(BaseContext.getCurrentId());
        for (Long registrationTypeId : scheduleTemplateDTO.getRegistrationTypes_Ids()) {
            scheduleMapper.insertTemplate(scheduleTemplateDTO, registrationTypeId);
        }
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

//    /**
//     * 查询排班信息
//     */
//    public List<Doctor_SchedulingVO> querySchedule(Long doctorId) {
//        List<Doctor_Scheduling> doctorSchedulingList = scheduleMapper.selectDoctorScheduleByDoctorId(doctorId);
//    }
//

}
