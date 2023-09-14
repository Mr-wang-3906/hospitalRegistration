package hospital.mapper;

import hospital.dto.ScheduleTemplateDTO;
import hospital.entity.Doctor_Scheduling;
import hospital.entity.Patient_Doctor_Scheduling;
import hospital.entity.ScheduleTemplate;
import hospital.temp.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ScheduleMapper {
    List<ScheduleTemplate> selectByRegistrationTypeId(Long registrationTypeId);

    List<Patient_Doctor_Scheduling> selectPatientScheduleByRegistrationTypeId(Long registrationTypeId);

    List<Doctor_Scheduling> selectDoctorScheduleByRegistrationTypeId(Long registrationTypeId);

    void insertTemplate(@Param("template") ScheduleTemplateDTO template, @Param("registrationTypeIds") String registrationTypeIds);

    List<ScheduleTemplate> selectByDoctorId(Long doctorId);

    ScheduleTemplate selectById(Long id);

    void deleteByTemplateId(Long id);

    void updateByRegistrationTypeId(Long registrationTypeId);

    void insertNewDoctorSchedule(@Param("doctorId") Long doctorId, @Param("date") Date date);

    List<Doctor_Scheduling> selectDoctorSchedulingTempByRegistrationTypeId(@Param("doctorId") Long doctorId, @Param("registrationTypeId") Long id);

    List<Doctor_Scheduling> selectDoctorScheduleByDoctorId(Long doctorId);

    void updateTemplate(@Param("template") ScheduleTemplateDTO template, @Param("registrationTypeIds") String registrationTypeIds);

    void updateByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") java.sql.Date date, @Param("ds") Doctor_Scheduling doctorScheduling);

    Doctor_Scheduling selectByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("Date") String sourceDate);

    void copyOneDay(@Param("targetDate") String targetDate, @Param("schedule") Doctor_Scheduling doctorScheduling, @Param("doctorId") Long doctorId);

    List<Patient_Doctor_Scheduling> selectPatientDoctorScheduling(java.sql.Date date);

    void updatePatientDoctorSchedulingDate();

    void updatePatientDoctorScheduling(@Param("doctorId") Long doctorId, @Param("date") Date date, @Param("registrationTypeIds") String registrationTypeIds);

    void deleteDoctorSchedulingMonth(int lastMonth);

    void insertNextMonthSchedules(@Param("doctorId") Long doctorId, @Param("nextDate") String nextDate);

    Patient_Doctor_Scheduling selectPatientDoctorSchedulingByIdAndDate(@Param("doctorId") Long doctorId, @Param("date") java.sql.Date date);

    void updateConfirmPaymentNine(@Param("order") Orders orders, @Param("estimatedTime") String estimatedTime);

    void updateConfirmPaymentTen(@Param("order") Orders orders, @Param("estimatedTime") String estimatedTime);

    void updateConfirmPaymentEleven(@Param("order") Orders orders, @Param("estimatedTime") String estimatedTime);

    void updateConfirmPaymentFourteen(@Param("order") Orders orders, @Param("estimatedTime") String estimatedTime);

    void updateConfirmPaymentFifTeen(@Param("order") Orders orders, @Param("estimatedTime") String estimatedTime);

    void updateConfirmPaymentSixTeen(@Param("order") Orders orders, @Param("estimatedTime") String estimatedTime);

    List<Patient_Doctor_Scheduling> selectPatientDoctorSchedulingByDoctorId(Long doctorId);

    Patient_Doctor_Scheduling selectPatientDoctorSchedulingByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") String date);

    void insertNewDoctorAppointment(@Param("newDoctorId") Long newDoctorId, @Param("date") String date);
}
