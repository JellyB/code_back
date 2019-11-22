package com.huatu.ztk.backend.util;

import org.apache.commons.lang3.RandomUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ht on 2016/11/24.
 */
public class DateFormat {

    public static String strTOYMD(String date){
        if(date!=null&&date.contains("Z")){
            date = date.replace("Z", " UTC");//注意是空格+UTC
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//注意格式化的表达式
            Date d = null;
            try {
                d = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dateTostr(d);
        }
       return date;
    }

    public static String dateTostr(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");//注意格式化的表达式
        return format.format(date);
    }
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    public static String transferLongToDate(String dateFormat, Long millSec) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(millSec);
        return sdf.format(date);
    }

    public static String getCurrentDate(){
        //得到long类型当前时间
        long l = System.currentTimeMillis();
        //new日期对象
        Date date = new Date(l);
        //转换提日期输出格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    public static void main(String[] args){
        int delta = RandomUtils.nextInt(1, 4);//随机步长
        String id=String.valueOf(System.currentTimeMillis()).substring(6)+delta;
        System.out.println(id);
        System.out.println(Integer.valueOf(id));

        System.out.println(new Date().getTime());
    }
}
