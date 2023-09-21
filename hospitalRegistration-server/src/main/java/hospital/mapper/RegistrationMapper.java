package hospital.mapper;

import hospital.entity.RegistrationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistrationMapper {
    void insertRegistrationType(@Param("doctorId") Long doctorId, @Param("rt") RegistrationType rt);

    List<RegistrationType> queryRegistrationType(Long doctorId);

    void updateRegistrationType(@Param("doctorId") Long doctorId, @Param("rt") RegistrationType rt);

    void deleteRegistrationType(Long id);

    RegistrationType selectById(Long registrationTypeId);

    RegistrationType queryRegistrationTypeOnlyOne(@Param("doctorId") Long doctorId, @Param("registrationName") String registrationName);

    List<RegistrationType> selecetByDoctorId(Long doctorId);

    RegistrationType selectByName(String registrationName);
}
