package com.huatu.tiku.schedule.biz.vo.CourseLivePackage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**返回日期封装
 * @author wangjian
 **/
@Data
public class DateFormatVo implements Serializable {

    private static final long serialVersionUID = -2318851169020222350L;
    private String dateFormat;

    private String dateValue;

    @JsonIgnore
    private final SimpleDateFormat dateSdf = new SimpleDateFormat("MM月dd日");

    @JsonIgnore
    private final SimpleDateFormat valueSdf = new SimpleDateFormat("yyyy-MM-dd");

    public DateFormatVo(Date date){
        dateFormat=dateSdf.format(date);
        dateValue=valueSdf.format(date);
    }
}
