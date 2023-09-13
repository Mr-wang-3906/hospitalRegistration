package hospital.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppointmentMapper {

    void setStatusOngoing(@Param("patientId") Long patientId, @Param("time")String time, @Param("doctorName")String doctorName, @Param("status")String status);

    void updateStatus(@Param("patientId")Long patientId, @Param("time")String orderTime, @Param("status")String status);
}
