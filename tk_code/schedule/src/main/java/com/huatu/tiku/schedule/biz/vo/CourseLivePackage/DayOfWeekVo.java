package com.huatu.tiku.schedule.biz.vo.CourseLivePackage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**返回星期封装
 * @author wangjian
 **/
@Data
public class DayOfWeekVo implements Serializable {

    private static final long serialVersionUID = 8002371597257587941L;
    private String dayOfWeek;

    @JsonIgnore
    private final SimpleDateFormat dayOfWeekSdf = new SimpleDateFormat("EEE", Locale.CHINA);
    public DayOfWeekVo(Date date){
        dayOfWeek=dayOfWeekSdf.format(date);
    }

}
