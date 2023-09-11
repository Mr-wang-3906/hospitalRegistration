package hospital.mapper;

import hospital.dto.ScheduleTemplateDTO;
import hospital.entity.Doctor_Scheduling;
import hospital.entity.Patient_Doctor_Scheduling;
import hospital.entity.ScheduleTemplate;
import hospital.temp.Doctor_Scheduling_Temp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ScheduleMapper {
    List<ScheduleTemplate> selectByRegistrationTypeId(Long registrationTypeId);

    List<Patient_Doctor_Scheduling> selectPatientScheduleByRegistrationTypeId(Long registrationTypeId);

    List<Doctor_Scheduling> selectDoctorScheduleByRegistrationTypeId(Long registrationTypeId);

    void insertTemplate(@Param("template") ScheduleTemplateDTO template, @Param("registrationTypeId") Long registrationTypeId);

    List<ScheduleTemplate> selectByDoctorId(Long doctorId);

    List<ScheduleTemplate> selectById(Long id);

    void deleteByTemplateId(Long id);

    void deleteByRegistrationTypeId(Long registrationTypeId);

    void insertNewDoctorSchedule(@Param("doctorId") Long doctorId,@Param("date") Date date);

    List<Doctor_Scheduling_Temp> selectDoctorSchedulingTempByRegistrationTypeId(@Param("doctorId") Long doctorId, @Param("registrationTypeId") Long id);

    List<Doctor_Scheduling> selectDoctorScheduleByDoctorId(Long doctorId);
}
