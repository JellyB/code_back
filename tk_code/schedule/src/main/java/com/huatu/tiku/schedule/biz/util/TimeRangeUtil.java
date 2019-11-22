package com.huatu.tiku.schedule.biz.util;

import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.TimeRangeVo;

import java.util.Set;

/**时间范围相关工具
 * @author wangjian
 **/
public class TimeRangeUtil {

    /**
     * 返回时间段在集合中的下标
     * @param set 时间范围集合
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @return 下标位置 从1开始
     */
    public static int getIndex(Set<TimeRangeVo> set, Integer timeBegin, Integer timeEnd){
        int i=0;
        if(null==timeBegin&&null==timeEnd){
            return 0;
        }
        for(TimeRangeVo timeRangeVo :set){
            if(timeRangeVo.getTimeBegin().equals(timeBegin)&& timeRangeVo.getTimeEnd().equals(timeEnd)){
                return ++i;
            }
            i++;
        }
        return 0;
    }

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
