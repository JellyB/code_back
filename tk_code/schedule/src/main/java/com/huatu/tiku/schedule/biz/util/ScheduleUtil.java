package com.huatu.tiku.schedule.biz.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.biz.vo.CourseLiveScheduleVo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**将课表转成日历格式
 * @author wangjian
 **/
public class ScheduleUtil {

    /**
     *
     * @param dateBegin 开始时间
     * @param dateEnd 结束时间
     * @param headers 日期字符串集合
     * @param datas 数据集合
     * @return 日历个数直播课表
     */
    public static ImmutableMap getResult(Date dateBegin,Date dateEnd,
                                         List<String> headers, List<List<CourseLiveScheduleVo>> datas){
        Calendar start = Calendar.getInstance();
        start.setTime(dateBegin);
        List<List<String>> resultHead= Lists.newArrayList();
        List<List<List<CourseLiveScheduleVo>>> resultBody=Lists.newArrayList();
        int day_of_week = start.get(Calendar.DAY_OF_WEEK);//周日=1 周一=2
        List<List<CourseLiveScheduleVo>> firseWeekBody=Lists.newArrayList();
        List<String> firseWeek=new ArrayList<>();
        for(int i=1;i<day_of_week;i++){
            firseWeekBody.add(Lists.newArrayList());
            firseWeek.add("");//第一天之前数据填充
        }
        int num=0;//计数器
        while(1!=start.get(Calendar.DAY_OF_WEEK)&&start.getTime().before(dateEnd)){//不是周日
            firseWeekBody.add(datas.get(num));
            firseWeek.add(headers.get(num++));//填入日期,并累加计数器
            start.roll(Calendar.DAY_OF_YEAR, true);//日期累加
        }
        resultBody.add(firseWeekBody);
        resultHead.add(firseWeek);//第一周

        List<List<CourseLiveScheduleVo>> tmpBody=null;
        List<String> tmp=null;
        for (; start.getTime().before(dateEnd); start.roll(Calendar.DAY_OF_YEAR, true)) {
            if(1==start.get(Calendar.DAY_OF_WEEK)){
                resultBody.add(tmpBody);
                resultHead.add(tmp);
                tmp=Lists.newArrayList();
                tmpBody=Lists.newArrayList();
            }
            tmpBody.add(datas.get(num));
            tmp.add(headers.get(num++));//填入日期,并累加计数器
        }
        resultHead.add(tmp);
        resultHead.remove(1);
        resultBody.add(tmpBody);
        resultBody.remove(1);
        return ImmutableMap.of("headers", resultHead, "datas", resultBody);
    }
}
