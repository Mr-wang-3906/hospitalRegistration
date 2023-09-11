package hospital.mapper;

import hospital.entity.Doctor;
import hospital.temp.DoctorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DoctorMapper {
    void insertNewDoctor(Doctor doctor);

    Doctor selectByUsername(String username);

    Doctor selectById(Long doctorId);

    void updateInfo(@Param("doctorId") Long doctorId, @Param("dI") DoctorInfo doctorInfo);
}
