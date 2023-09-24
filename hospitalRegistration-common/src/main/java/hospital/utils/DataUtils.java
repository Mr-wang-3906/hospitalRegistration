package hospital.utils;

import hospital.exception.AllException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

@SuppressWarnings("all")
public final class DataUtils {
    /**
     * 预设不同的时间格式
     */
    //精确到年月日（英文） eg:2019-11-11
    public static String FORMAT_LONOGRAM = "yyyy-MM-dd";
    //精确到时分秒的完整时间（英文） eg:2010-11-11 12:12:12
    public static String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
    //精确到毫秒完整时间（英文） eg:2019-11-11 12:12:12.55
    public static String FORMAT_FULL_MILL = "yyyy-MM-dd HH:mm:ss.SSS";
    //精确到年月日（中文）eg:2019年11月11日
    public static String FORMAT_LONOGRAM_CN = "yyyy年MM月dd日";
    //精确到时分秒的完整时间（中文）eg:2019年11月11日 12时12分12秒
    public static String FORMAT_FULL_CN = "yyyy年MM月dd日 HH时mm分ss秒";
    //精确到毫秒完整时间（中文）
    public static String FORMAT_FULL_MILL_CN = "yyyy年MM月dd日 HH时mm分ss秒SSS毫秒";

    /**
     * 预设默认的时间格式
     */
    public static String getDefaultFormat() {
        return FORMAT_FULL;
    }


    /**
     * 预设格式格式化日期
     */
    public static String format(Date date) {
        return format(date, getDefaultFormat());
    }

    /**
     * 自定义格式格式化日期
     */
    public static String format(Date date, String format) {
        String value = "";
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            value = sdf.format(date);
        }
        return value;
    }

    /**
     * 根据预设默认格式，返回当前日期
     */
    public static String getNow() {
        return format(new Date());
    }

    /**
     * 自定义时间格式，返回当前日期
     */
    public static String getNow(String format) {
        return format(new Date(), format);
    }

    /**
     * 自定义时间格式：String->Date
     */
    public static Date parse(String strDate, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 基于指定日期增加年
     *
     * @param num 整数往后推，负数往前移
     */
    public static Date addYear(Date date, int num) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, num);
        return cal.getTime();
    }

    /**
     * 基于指定日期增加整月
     *
     * @param num 整数往后推，负数往前移
     */
    public static Date addMonth(Date date, int num) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, num);
        return cal.getTime();
    }

    /**
     * 基于指定日期增加天数
     *
     * @param num 整数往后推，负数往前移
     */
    public static Date addDay(Date date, int num) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, num);
        return cal.getTime();
    }

    /**
     * 基于指定日期增加分钟
     *
     * @param num 整数往后推，负数往前移
     */
    public static Date addMinute(Date date, int num) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, num);
        return cal.getTime();
    }

    /**
     * 获取时间戳 eg:yyyy-MM-dd HH:mm:ss.S
     */
    public static String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_FULL_MILL);
        Calendar cal = Calendar.getInstance();
        return sdf.format(cal.getTime());
    }

    /**
     * 获取日期年份
     */
    public static String getYear(Date date) {
        return format(date).substring(0, 4);
    }

    /**
     * 获取年份+月
     */
    public static String getYearMonth(Date date) {
        return format(date).substring(0, 7);
    }

    /**
     * 获取日期的小时数
     */
    public static int getHour(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 自定义时间格式字符串距离今天的天数
     */
    public static int countDays(String strDate, String format) {
        long time = Calendar.getInstance().getTime().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(Objects.requireNonNull(parse(strDate, format)));
        long diff = cal.getTime().getTime();
        return (int) (time / 1000 - diff / 1000) / 3600 / 24;
    }

    /**
     * 预设格式的字符串距离今天的天数
     */
    public static int countDays(String strDate) {
        return countDays(strDate, getDefaultFormat());
    }

    /**
     * 获取天数差值(依赖时间)
     *
     * @return
     */
    public static int diffDays(Date date1, Date date2) {
        if (date1 == null || date2 == null) return 0;
        return (int) (Math.abs(date1.getTime() - date2.getTime()) / (60 * 60 * 24 * 1000));
    }

    /**
     * 获取年份差值
     */
    public static int diffYear(Date year1, Date year2) {
        return diffDays(year1, year2) / 365;
    }

    /**
     * 获取天数差值(依赖Date类型的日期)
     *
     * @return
     */
    public static int diffByDays(Date d1, Date d2) {
        Date s1 = parse(format(d1, FORMAT_LONOGRAM), FORMAT_LONOGRAM);
        Date s2 = parse(format(d2, FORMAT_LONOGRAM), FORMAT_LONOGRAM);
        return diffDays(s1, s2);
    }

    /**
     * 获取时间分割集合
     *
     * @param date  查询日期
     * @param stars 带拆分的时间点
     * @return
     */
    public static List<Date> collectTimes(Date date, String[] stars) {
        List<Date> result = new ArrayList<>();
        List<String> times = Arrays.asList(stars);
        String dateStr = format(date, FORMAT_LONOGRAM);
        String pattern = FORMAT_LONOGRAM + " k";
        if (times.size() > 0) {
            times.forEach(t -> {
                result.add(parse(dateStr + " " + t, pattern));
            });
        }
        return result;
    }

    /**
     * 根据日期查询当前为周几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"1", "2", "3", "4", "5", "6", "7"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK);
        if (0 == w) {
            w = 7;
        }
        return weekDays[w];
    }

    public static String intToCn(int hourInt, String[] timeArray) {
        String result = "";
        if (0 <= hourInt && hourInt <= 10) {
            result += timeArray[hourInt] + "\n";
        } else if (11 <= hourInt && hourInt <= 19) {
            result += (timeArray[10] + "\n" + timeArray[hourInt % 10]) + "\n";
        } else {
            result += (timeArray[hourInt / 10] + "\n" + timeArray[10] + "\n" + (hourInt % 10 == 0 ? "" : timeArray[hourInt % 10] + "\n"));
        }
        return result;
    }

    /**
     * 将时间转换成汉字
     *
     * @param hour
     * @return
     */
    public static String hourToCn(String hour) {
        String[] timeArray = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        String[] hourArray = hour.split(":");
        int hourInt = Integer.parseInt(hourArray[0]);
        int minute = Integer.parseInt(hourArray[1]);
        return intToCn(hourInt, timeArray) + "点\n" + intToCn(minute, timeArray) + "分";
    }


    /**
     * 获取当月 + 整数(正数往后,负数往前)月 的所有天
     *
     * @return
     */
    public static List<String> getDayListOfMonth(int number) {
        List<String> list = new ArrayList<String>();
        YearMonth yearMonth = YearMonth.now().plusMonths(number);
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int i = 1; i <= daysInMonth; i++) {
            String aDate = String.format("%d-%02d-%02d", year, month, i);
            list.add(aDate);
        }

        return list;
    }

    /**
     * 获取过去 x天到今天 的日期
     *
     * @param intervals
     * @return
     */
    public static ArrayList<String> pastDaysList(int intervals) {
        ArrayList<String> pastDaysList = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            pastDaysList.add(getPastDate(i));
        }
        Collections.reverse(pastDaysList);
        return pastDaysList;
    }

    /**
     * 获取 今天到未来x天 的日期
     *
     * @param intervals
     * @return
     */
    public static ArrayList<String> futureDaysList(int intervals) {
        ArrayList<String> futureDaysList = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            futureDaysList.add(getFutureDate(i));
        }
        return futureDaysList;
    }

    //得到过去日期
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(today);
    }

    //得到未来日期
    public static String getFutureDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(today);
    }

    /**
     * 根据年 月数 得到所包含的周数
     **/
    public static int getWeekCount(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        TemporalField weekOfYear = WeekFields.ISO.weekOfYear();

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int startWeek = firstDayOfMonth.get(weekOfYear);

        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();
        int endWeek = lastDayOfMonth.get(weekOfYear);

        // 处理跨年的情况
        if (endWeek < startWeek) {
            int weeksInPreviousYear = YearMonth.of(year - 1, month).atEndOfMonth().get(weekOfYear);
            endWeek += weeksInPreviousYear;
        }

        return endWeek - startWeek;
    }

    /**
     * 根据年 周数 得到该周日期数
     *
     * @param year
     * @param week
     * @return
     */
    public static List<LocalDate> getWeekDates(int year, int week) {
        List<LocalDate> weekDates = new ArrayList<>();

        TemporalField weekOfYear = WeekFields.ISO.weekOfYear();

        LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
        int firstWeekOfYear = firstDayOfYear.get(weekOfYear);

        LocalDate firstDayOfRequestedWeek = firstDayOfYear.with(ChronoField.ALIGNED_WEEK_OF_YEAR, week).plusDays(1);
        LocalDate lastDayOfRequestedWeek = firstDayOfRequestedWeek.plusDays(6);

        // 处理跨年的情况
        if (week < firstWeekOfYear) {
            firstDayOfRequestedWeek = firstDayOfRequestedWeek.plusWeeks(52);
            lastDayOfRequestedWeek = firstDayOfRequestedWeek.plusDays(6);
        }

        // 将每一天添加到列表中
        LocalDate currentDate = firstDayOfRequestedWeek;
        while (!currentDate.isAfter(lastDayOfRequestedWeek)) {
            weekDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return weekDates;
    }

    /**
     * 获取某年某月所有的日期
     *
     * @param year
     * @param month
     * @return
     */
    public static List<LocalDate> getMonthDates(int year, int month) {
        List<LocalDate> monthDates = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atDay(daysInMonth);

        // 将每一天添加到列表中
        LocalDate currentDate = firstDayOfMonth;
        while (!currentDate.isAfter(lastDayOfMonth)) {
            monthDates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return monthDates;
    }

    //将 HH-MM 转换成 YYYY-MM-DD HH:MM:00
    public static String convertTimeFormat(String time) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = today.format(formatter);

        String[] parts = time.split("-");
        String startTime = parts[0] + ":00";
        String endTime = parts[1] + ":00";

        return formattedDate + " " + startTime + "-" + endTime;
    }

    //对字符串日期数组进行月的替换
    public static ArrayList<String> replaceMonth(ArrayList<String> dates, String monthString) {
        int month = Integer.parseInt(monthString);
        ArrayList<String> modifiedDates = new ArrayList<>();

        for (String date : dates) {
            LocalDate localDate = LocalDate.parse(date);
            LocalDate modifiedDate = localDate.withMonth(month);
            modifiedDates.add(modifiedDate.toString());
        }

        return modifiedDates;
    }

    //将redis剩余过期时间转化为mm:ss形式
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d:%02d", minutes, seconds);
    }


}
