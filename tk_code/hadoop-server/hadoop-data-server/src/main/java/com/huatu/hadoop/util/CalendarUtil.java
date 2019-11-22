package com.huatu.hadoop.util;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarUtil {

    private final static SimpleDateFormat DATE_FORMAT_FROM = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) throws ParseException {
        String tody = "20190601";
        Calendar currentDate = new GregorianCalendar();

        currentDate.setTime(new SimpleDateFormat("yyyyMMdd").parse(tody));
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);

        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.DATE, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_WEEK, currentDate.getFirstDayOfWeek());
        long time = currentDate.getTime().getTime();



        System.out.println(time);
    }


    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyw");

    private static String getWeek(String str) throws ParseException {


        return Long.toString(sdf2.parse(sdf2.format(new Date(sdf.parse(str).getTime() - 24 * 60 * 60 * 1000L))).getTime());
    }


    public static long getLastWeek(String today) throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date date = format.parse(today);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
//        System.out.println(calendar.get(Calendar.WEEK_OF_YEAR));
        return calendar.getTime().getTime();
    }

    public static String getWeekStart(String today) throws ParseException {

        Calendar currentDate = new GregorianCalendar();
        currentDate.setTime(new SimpleDateFormat("yyyyMMdd").parse(today));

        currentDate.setFirstDayOfWeek(Calendar.MONDAY);

        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.DAY_OF_WEEK, currentDate.getFirstDayOfWeek());
        long time = currentDate.getTime().getTime();

        String s = Long.toString(time);

        s = s.substring(0, s.length() - 3) + "000";

        return s;
    }

    public static String getWeekend(String today) throws ParseException {

        Calendar currentDate = new GregorianCalendar();
        currentDate.setTime(new SimpleDateFormat("yyyyMMdd").parse(today));
        currentDate.setFirstDayOfWeek(Calendar.MONDAY);


        currentDate.set(Calendar.HOUR_OF_DAY, 23);
        currentDate.set(Calendar.MINUTE, 59);
        currentDate.set(Calendar.SECOND, 59);
        currentDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long time = currentDate.getTime().getTime();
        String s = Long.toString(time);

        s = s.substring(0, s.length() - 3) + "000";

        return s;
    }

    public static Object countTwoDate(String startDate, String endDate) throws ParseException {

        if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
            Date start = DATE_FORMAT_FROM.parse(startDate);
            Date end = DATE_FORMAT_FROM.parse(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            long time1 = cal.getTimeInMillis();
            cal.setTime(end);
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        }
        return null;
    }

    public static Object countTwoDayWeek(String startDate, String endDate) throws ParseException {
        if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
            Date start = DATE_FORMAT_FROM.parse(startDate);
            Date end = DATE_FORMAT_FROM.parse(endDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            long time1 = cal.getTimeInMillis();
            cal.setTime(end);
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            Double days = Double.parseDouble(String.valueOf(between_days));
            if ((days / 7) > 0 && (days / 7) <= 1) {
                //不满一周的按一周算
                return 1;
            } else if (days / 7 > 1) {
                int day = days.intValue();
                if (day % 7 > 0) {
                    return day / 7 + 1;
                } else {
                    return day / 7;
                }
            } else if ((days / 7) == 0) {
                return 0;
            } else {
                //负数返还null
                return null;
            }
        }
        return null;
    }

    public static int getMonthDiff(String d1, String d2) throws ParseException {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        //将String日期转换成date

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        java.util.Date date1 = sdf.parse(d1);
        java.util.Date date2 = sdf.parse(d2);
        c1.setTime(date1);
        c2.setTime(date2);

        //判断两个日期的大小

        if (c2.getTimeInMillis() < c1.getTimeInMillis()) return 0;
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        // 获取年的差值 假设 d1 = 2015-9-30   d2 = 2015-12-16
        int yearInterval = year2 - year1;
        // 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
        if (month2 < month1 || month1 == month2 && day2 < day1) yearInterval--;
        // 获取月数差值
        int monthInterval = (month2 + 12) - month1;
        if (day2 > day1) monthInterval++;
        monthInterval %= 12;
        return yearInterval * 12 + monthInterval;
    }


}
