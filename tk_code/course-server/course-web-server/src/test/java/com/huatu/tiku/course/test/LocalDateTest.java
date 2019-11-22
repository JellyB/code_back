package com.huatu.tiku.course.test;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-06 6:14 PM
 **/
public class LocalDateTest {

    public static void main(String[] args) {
        LocalDate localDate = LocalDate.now().minusDays(1);
        String currentKey = localDate.toString();
        System.err.println(">>>>>>>>>>>" + currentKey);
        /*DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
        String result = LocalDate.now().format(dateTimeFormatter);
        System.err.println("current time format " + result);*/
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println("simple date format:" + simpleDateFormat.format(new Date()));
    }
}
