package com.huatu.ztk.backend.metas.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2018\3\20 0020.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchUserBean {
    private long id;
    //模考大赛
    private int paperId;

    private String paperName;
    //地区维度
    private int areaId;
    //地区名称
    private String areaName;
    //科目维度
    private int subjectId;
    //用户维度
    private long userId;
    private String userName;
    //报名行为（0否1是）
    private int enrolled;
    //查看行为（0否1是）
    private int looked;
    //答题卡
    private long practiceId;
    //答题行为（0否1是）
    private int joined;
    //交卷行为(时间)
    private long submitTime;
    //试卷开始时间
    private long startTime;
    //试卷结束时间
    private long endTime;
    //得分数据
    private double score;
    //全站排名数据
    private String order;
    //地区排名数据
    private String orderByArea;

}
