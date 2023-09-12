package hospital.mapper;

import hospital.entity.Doctor;
import hospital.entity.Doctor_Scheduling;
import hospital.temp.DoctorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {
    void insertNewDoctor(Doctor doctor);

    Doctor selectByUsername(String username);

    Doctor selectById(Long doctorId);

    void updateInfo(@Param("doctorId") Long doctorId, @Param("dI") DoctorInfo doctorInfo);

    void updateOneDaySchedule(@Param("doctorScheduling") Doctor_Scheduling doctorScheduling, @Param("data")String data);

    Doctor selectByIdAndSection(@Param("doctorId") Long doctor, @Param("section") String section);

    List<Doctor> selectAll();
}
