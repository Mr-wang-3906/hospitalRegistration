package hospital.service;

import hospital.dto.LoginDTO;
import hospital.dto.DoctorRegisterDTO;
import hospital.dto.ScheduleTemplateDTO;
import hospital.entity.Doctor;
import hospital.entity.Doctor_Scheduling;
import hospital.entity.RegistrationType;
import hospital.temp.DoctorInfo;
import hospital.vo.Doctor_SchedulingVO;
import hospital.vo.ScheduleTemplateVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface DoctorService {
    void insertNewDoctor(DoctorRegisterDTO doctorRegisterDTO, HttpServletRequest httpServletRequest);

    Doctor login(LoginDTO loginDTO);

    void addRegistrationType(RegistrationType registrationType);

    List<RegistrationType> queryRegistrationType();

    void updateRegistrationType(RegistrationType registrationType);

    void deleteRegistrationType(List<Long> ids);

    void addTemplate(ScheduleTemplateDTO scheduleTemplateDTO);

    List<ScheduleTemplateVO> queryTemplate(Long doctorId);

    void deleteTemplates(List<Long> ids);

    void updateTemplate(ScheduleTemplateDTO scheduleTemplateDTO);

    DoctorInfo queryInfoById(Long doctorId);

    void updateInfo(DoctorInfo doctorInfo);

//    List<Doctor_SchedulingVO> querySchedule(Long doctorId);
}
