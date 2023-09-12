package hospital.service;

import hospital.dto.LoginDTO;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.entity.RegistrationType;
import hospital.temp.PatientInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PatientService {

    Patient login(LoginDTO loginDTO);

    void insertNewPatient(PatientRegisterDTO patientRegisterDTO, HttpServletRequest httpServletRequest);

    PatientInfo queryInfo(Long patientId);

    List<Doctor> checkRegistration(PatientCheckRegistrationDTO patientCheckRegistrationDTO);

    List<RegistrationType> choiceDoctor(Long doctor);
}
