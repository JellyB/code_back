package com.huatu.tiku.position.biz.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class ScoreLineVo implements Serializable{

    private static final long serialVersionUID = -5766010631528495920L;

    private Integer year;

    private BigDecimal interviewScope;//面试分数

    private Integer count;//报名人数

    private Integer number;//招录人数

    private String proportion;//报录比

    @JsonIgnore
    private Integer yearCount=1;//个数 默认1

}
