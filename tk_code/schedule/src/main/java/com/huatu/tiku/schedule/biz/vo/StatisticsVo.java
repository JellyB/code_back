package com.huatu.tiku.schedule.biz.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wangjian
 **/
@Data
public class StatisticsVo  implements Serializable{

    private static final long serialVersionUID = 5593970769878150819L;

    private String date;

    private String count;

    private String time;

    private String liveName;

    private Float coefficient;

    private String categoryName;

    private String courseName;
}
