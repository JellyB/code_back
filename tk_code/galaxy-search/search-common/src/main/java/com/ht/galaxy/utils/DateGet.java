package com.ht.galaxy.utils;


import org.joda.time.DateTime;

/**
 * @author jbzm
 * @date Create on 2018/3/16 14:04
 */

public class DateGet {

    public Long getYear(DateTime date) {
        String s = date.toString().substring(0, 4);
        return new DateTime(s).getMillis() ;
    }

    public Long getMonth(DateTime date) {
        String s = date.toString().substring(0, 7);
        return new DateTime(s).getMillis() ;
    }

    public Long getWeek(DateTime date) {
        return (new DateTime().getMillis() - (new DateTime().getDayOfWeek() * 24 * 60 * 60 * 1000));
    }

    public Long getDay(DateTime date) {
        String s = date.toString().substring(0, 10);
        return new DateTime(s).getMillis()-24*1000*60*60 ;
    }

    public Long getNew(DateTime date) {
        return date.getMillis() ;
    }

}
