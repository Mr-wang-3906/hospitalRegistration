package hospital.service;

import hospital.dto.*;
import hospital.entity.AppointmentRecords;
import hospital.entity.Doctor;
import hospital.entity.RegistrationType;
import hospital.temp.DoctorInfo;
import hospital.temp.Doctor_SchedulingTemp;
import hospital.temp.PatientAppointmentInfo;
import hospital.vo.Doctor_SchedulingVO;
import hospital.vo.ScheduleTemplateVO;

import java.util.ArrayList;
import java.util.List;

public interface DoctorService {
    void insertNewDoctor(DoctorRegisterDTO doctorRegisterDTO);

    Doctor login(LoginDTO loginDTO);

    void addRegistrationType(RegistrationType registrationType);

    List<RegistrationType> queryRegistrationType();

    void updateRegistrationType(RegistrationType registrationType);

    void deleteRegistrationType(List<Long> ids);

    Long addTemplate(ScheduleTemplateDTO scheduleTemplateDTO);

    List<ScheduleTemplateVO> queryTemplate(Long doctorId);

    void deleteTemplates(List<Long> ids);

    void updateTemplate(ScheduleTemplateDTO scheduleTemplateDTO);

    DoctorInfo queryInfoById(Long doctorId);

    void updateInfo(DoctorInfo doctorInfo);

    ArrayList<Doctor_SchedulingVO> querySchedule(Long doctorId);

    void setScheduleWithTemplate(DoctorSetScheduleWithTemplate doctorSetScheduleWithTemplate);

    void updateOneDaySchedule(Doctor_SchedulingTemp doctorSchedulingTemp);

//    void copyScheduleWeek_abandon(ScheduleCopy_week scheduleCopyWeek);

    void copyScheduleWeek(ScheduleCopy_week weekNumber);

    void copyScheduleMonth(ScheduleCopy_month scheduleCopyMonth);

    void copyScheduleDay(ScheduleCopy_day scheduleCopyDay);

    void deliverRegistration();

    List<PatientAppointmentInfo> registrationCheck(String date);

    void setPatientCredit(PatientAppointment_PatientInfoDTO patientAppointmentInfoDTO);

    List<AppointmentRecords> queryPatientAppointment(Long patientId);

    void updatePassword(UpdatePasswordDTO updatePasswordDTO);
}
