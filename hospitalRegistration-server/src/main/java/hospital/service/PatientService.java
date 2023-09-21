package hospital.service;

import hospital.dto.LoginDTO;
import hospital.dto.PatientCheckRegistrationDTO;
import hospital.dto.PatientRegisterDTO;
import hospital.entity.Doctor;
import hospital.entity.Patient;
import hospital.temp.Orders;
import hospital.temp.PatientInfo;
import hospital.vo.Patient_Doctor_SchedulingVO;


import java.util.List;

public interface PatientService {

    Patient login(LoginDTO loginDTO);

    void insertNewPatient(PatientRegisterDTO patientRegisterDTO);

    PatientInfo queryInfo(Long patientId);

    List<Doctor> checkRegistration(PatientCheckRegistrationDTO patientCheckRegistrationDTO);

    List<Patient_Doctor_SchedulingVO> choiceDoctor(Long doctor);

    String choiceTime(Orders orders);

    void confirmPayment(Orders orders);

    void cancelPayment(Orders orders);
}
