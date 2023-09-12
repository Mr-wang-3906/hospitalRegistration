import hospital.utils.DataUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

}

