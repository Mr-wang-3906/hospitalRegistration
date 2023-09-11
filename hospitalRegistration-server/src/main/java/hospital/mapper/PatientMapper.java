package hospital.mapper;

import hospital.entity.Doctor;
import hospital.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientMapper {

    Patient selectByUsername(String username);

    void insertPatient(Patient patientTemp);

    Patient selectById(Long patientId);
}
