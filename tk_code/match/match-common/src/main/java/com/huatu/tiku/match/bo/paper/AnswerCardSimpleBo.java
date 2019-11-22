package com.huatu.tiku.match.bo.paper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建答题卡返回信息
 * Created by lijun on 2019/1/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AnswerCardSimpleBo implements Serializable {

    private Long id;
    private String idStr;

    private String name;

    //总消耗时间
    private Integer expendTime;
    //剩余时间
    private Integer remainingTime;
    //开始时间
    private Long startTime;
    //结束时间
    private Long endTime;

    //当前时间
    private Long currentTime;

    //错误回调
    private String matchErrorPath;

}
