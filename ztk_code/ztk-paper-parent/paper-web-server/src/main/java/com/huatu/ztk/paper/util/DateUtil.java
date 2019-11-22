package com.huatu.ztk.paper.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-27 下午5:57
 **/
public class DateUtil {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMdd");
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
     return   dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("Asia/Shanghai")));
    }

    public static void main(String[] args) {
        System.out.println(getTodayEndMillions());
    }

}
