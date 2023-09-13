package hospital.service;

import hospital.dto.LoginDTO;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.entity.Patient_Doctor_Scheduling;
import hospital.temp.Orders;
import hospital.temp.PatientInfo;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface PatientService {

    Patient login(LoginDTO loginDTO);

    void insertNewPatient(PatientRegisterDTO patientRegisterDTO, HttpServletRequest httpServletRequest);

    PatientInfo queryInfo(Long patientId);

    List<Doctor> checkRegistration(PatientCheckRegistrationDTO patientCheckRegistrationDTO);

    List<Patient_Doctor_Scheduling> choiceDoctor(Long doctor);

    void choiceTime(Orders orders);

    void confirmPayment(Orders orders);
}
