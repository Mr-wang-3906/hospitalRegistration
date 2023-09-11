import hospital.utils.DataUtils;


import java.util.LinkedList;
import java.util.List;

public class JavaTest {
    public static void main(String[] args) {
        List<String> nextMonth = DataUtils.getDayListOfMonth(1);
        List<String> thisMonth = DataUtils.getDayListOfMonth(0);
        List<String> lastMonth = DataUtils.getDayListOfMonth(-1);
//        for (String s : lastMonth) {
//            System.out.println(s);
//        }
//        System.out.println("==================");
//        for (String s : thisMonth) {
//            System.out.println(s);
//        }
//        System.out.println("==================");
//        for (String s : nextMonth) {
//            System.out.println(s);
//

        List<String> Months = new LinkedList<>();
        Months.addAll(lastMonth);
        Months.addAll(thisMonth);
        Months.addAll(nextMonth);
        for (String month : Months) {
            System.out.println(month);
        }
    }
}
