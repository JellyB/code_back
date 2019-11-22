package com.huatu.tiku.schedule.biz.vo.CourseLivePackage;

import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;

/**时间范围封装
 * @author wangjian
 **/
@Data
public class TimeRangeVo implements Serializable{
    private static final long serialVersionUID = 5424678065266605132L;
    private Integer timeBegin;//起始时间
    private Integer timeEnd;//结束时间



    public static class MyComparator implements Comparator<TimeRangeVo> {
        @Override
        public int compare(TimeRangeVo o1, TimeRangeVo o2) {
            if(o1.getTimeBegin()==null&&o2.getTimeBegin()==null){
                return 0;
            }
            int num=o1.getTimeBegin()-o2.getTimeBegin();
            return num==0?o1.getTimeEnd()-o2.getTimeEnd():num;
        }
    }
}
