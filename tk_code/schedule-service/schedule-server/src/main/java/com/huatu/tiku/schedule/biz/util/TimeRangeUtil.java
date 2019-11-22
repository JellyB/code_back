package com.huatu.tiku.schedule.biz.util;


/**时间范围相关工具
 * @author wangjian
 **/
public class TimeRangeUtil {

    /**
     * 将int时间装成String
     * @param time 时间
     * @return String类型时间12:00
     */
    public static String intToDateString(Integer time){
        String timeString=String.valueOf(time);
        int length = timeString.length();
        switch (length){//补齐四位
            case 1:
                timeString="000"+timeString;
                break;
            case 2:
                timeString="00"+timeString;
                break;
            case 3:
                timeString="0"+timeString;
                break;
            case 4:
                break;
        }
        String result=timeString.substring(0,2)+":"+timeString.substring(2);
        return result;
    };
}
