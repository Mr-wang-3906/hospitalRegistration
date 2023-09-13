package hospital.mapper;

import hospital.dto.PatientAppointmentInfoDTO;
import hospital.entity.AppointmentRecords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppointmentMapper {

    void setStatusOngoing(@Param("patientId") Long patientId, @Param("time")String time, @Param("doctorName")String doctorName, @Param("status")String status);

    void updateStatus(@Param("patientId")Long patientId, @Param("time")String orderTime, @Param("status")String status);

    List<AppointmentRecords> selectByDoctorName(String name);

    int countNo_ShowNumber(Long patientId);

    void setStatusFinashed(@Param("name") String name, @Param("p") PatientAppointmentInfoDTO patientAppointmentInfoDTO);
}
