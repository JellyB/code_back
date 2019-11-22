package com.huatu.tiku.essay.util.date;

import javax.xml.ws.Holder;
import java.util.Date;

/**
 * Created by duanxiangchao on 2019/7/15
 */
public class DateCompareUtil {


    private static final Integer SECOND = 1000;
    private static final Integer MINUTE = 1000 * 60;
    private static final Integer HOUR   = 1000 * 60 * 60;
    private static final Integer DAY    = 1000 * 60 * 60 * 24;

    public static String getDuration(Date beginTime, Date endTime){
        Long begin = beginTime.getTime();
        Long end = endTime.getTime();
        StringBuffer stringBuffer = new StringBuffer();
        Long duration = end - begin;
        Long limit = duration;
        if(duration / DAY > 0){
            stringBuffer.append(duration / DAY + "天");
            limit = duration % DAY;
        }
        if(limit / HOUR > 0){
            stringBuffer.append(limit / HOUR + "小时");
            limit = limit % HOUR;
        }
        if(limit / MINUTE > 0){
            stringBuffer.append(limit / MINUTE + "分");
        }
        return stringBuffer.toString();
    }

}
