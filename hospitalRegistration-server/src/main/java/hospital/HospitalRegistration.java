package hospital;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching //开启缓存注解
@EnableScheduling //开启定时任务
public class HospitalRegistration {
    public static void main(String[] args) {
        SpringApplication.run(HospitalRegistration.class, args);
        log.info("server started");
    }
}
