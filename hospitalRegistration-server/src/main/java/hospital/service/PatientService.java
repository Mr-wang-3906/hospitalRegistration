package hospital.service;

import hospital.dto.LoginDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.Patient;
import hospital.temp.PatientInfo;

import javax.servlet.http.HttpServletRequest;

public interface PatientService {

    Patient login(LoginDTO loginDTO);

    void insertNewPatient(PatientRegisterDTO patientRegisterDTO, HttpServletRequest httpServletRequest);

    PatientInfo queryInfo(Long patientId);
}
