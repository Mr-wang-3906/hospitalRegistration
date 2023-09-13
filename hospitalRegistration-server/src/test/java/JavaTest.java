import hospital.utils.DataUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class JavaTest {
    ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);
    public static void main(String[] args) {
        List<LocalDate> weekDates = DataUtils.getWeekDates(2023, 37);
        System.out.println(weekDates);

        ArrayList<String> futureDaysList = DataUtils.futureDaysList(7);
        System.out.println(futureDaysList);

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);
        System.out.println(previousMonth.getMonthValue());
    }
    // 获取服务器的全局线程池
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

}

