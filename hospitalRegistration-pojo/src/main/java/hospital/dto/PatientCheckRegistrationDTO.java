package hospital.dto;

import lombok.Data;

import java.sql.Date;

@Data

public class PatientCheckRegistrationDTO {
   //科室
    private String section;

    //时间
    private Date date;
}
