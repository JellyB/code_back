package com.huatu.tiku.match.util;

import com.huatu.tiku.match.bo.MatchBo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author biguodong
 * Create time 2018-10-16 下午1:21
 **/
public class DateUtil {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMdd");
    public static final SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd");


    public static final SimpleDateFormat COURSE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    /**
     * 当天凌晨的00：00：00 的时间戳
     * @return
     */
    public static long getTodayStartMillions(){
        long now = System.currentTimeMillis() / 1000L;
        long daySecond = 60 * 60 * 24;
        return (now - (now + 8 * 3600) % daySecond)*1000;
    }
    /**
     * 当天凌晨的00：00：00 的时间戳
     * @return
     */
    public static long getTodayEndMillions(){
        long now = System.currentTimeMillis() / 1000L;
        long daySecond = 60 * 60 * 24;
        return (now - (now + 8 * 3600) % daySecond+daySecond)*1000;
    }

    public static  String getFormatDateString(long date) {
     return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("Asia/Shanghai")));
    }

    /**
     * 处理match timeInfo
     * @param matchBo
     */
    public static void packageTimeInfoWithEssay(MatchBo matchBo) {
        long startTime = matchBo.getStartTime();
        long endTime = matchBo.getEndTime();
        long essayStartTime = matchBo.getEssayStartTime();
        long essayEndTime = matchBo.getEssayEndTime();
        String timeInfo = "行测" + DateUtil.getTimeInfo(startTime, endTime) + "\n申论" + DateUtil.getTimeInfo(essayStartTime, essayEndTime);
        matchBo.setTimeInfo(timeInfo);
    }
    /**
     * 获取模考大赛考试时间信息
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getTimeInfo(long startTime, long endTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(startTime));

        /**
         * 考试时间：2017年8月20日（周日）09:00-11:00
         */
        String timeInfo = DateFormatUtils.format(startTime, "yyyy年M月d日") + "（%s）%s-%s";
        String dayString = getDayInfo(instance);
        timeInfo = String.format(timeInfo, dayString, DateFormatUtils.format(startTime, "HH:mm"),
                DateFormatUtils.format(endTime, "HH:mm"));

        return "：" + timeInfo;
    }

    /**
     * 返回每周 day 对应的中文
     * @param calendar
     * @return
     */
    public static final String getDayInfo(Calendar calendar){
        String dayString = StringUtils.EMPTY;
        if(null == calendar){
            return dayString;
        }

        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "周日";
                break;

            case Calendar.MONDAY:
                dayString = "周一";
                break;

            case Calendar.TUESDAY:
                dayString = "周二";
                break;
            case Calendar.WEDNESDAY:
                dayString = "周三";
                break;
            case Calendar.THURSDAY:
                dayString = "周四";
                break;
            case Calendar.FRIDAY:
                dayString = "周五";
                break;

            case Calendar.SATURDAY:
                dayString = "周六";
                break;
                default:
                    break;
        }
        return dayString;
    }

}
