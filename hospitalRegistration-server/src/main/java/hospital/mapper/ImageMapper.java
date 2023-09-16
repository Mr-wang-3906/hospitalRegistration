package hospital.mapper;

import hospital.entity.Doctor;
import hospital.entity.Doctor_Scheduling;
import hospital.entity.Image;
import hospital.temp.DoctorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImageMapper {

    void save(Image image);
}
