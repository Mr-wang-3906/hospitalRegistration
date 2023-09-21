package hospital.mapper;

import hospital.dto.PatientAppointment_PatientInfoDTO;
import hospital.entity.AppointmentRecords;
import hospital.temp.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppointmentMapper {

    void setStatusOngoing(@Param("patientId") Long patientId, @Param("order") Orders orders, @Param("doctorName")String doctorName, @Param("status")String status, @Param("oddNumber") String oddNumber);

    void updateStatus(@Param("patientId")Long patientId, @Param("time")String orderTime, @Param("status")String status, @Param("section") String section, @Param("oddNumber") String oddNumber);

    List<AppointmentRecords> selectByDoctorName(String name);

    int countNo_ShowNumber(Long patientId);

    void setStatusFinashed(@Param("patientAppointmentInfoDTO") PatientAppointment_PatientInfoDTO patientAppointmentInfoDTO);

    List<AppointmentRecords> selectByPatientId(Long patientId);

}
