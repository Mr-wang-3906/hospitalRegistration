package hospital.mapper;

import hospital.dto.PatientAppointment_PatientInfoDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PatientMapper {

    Patient selectByUsername(String username);

    void insertPatient(Patient patientTemp);

    Patient selectById(Long patientId);

    void updatePatient(@Param("patientAppointmentInfoDTO") PatientAppointment_PatientInfoDTO patientAppointmentInfoDTO);
}
