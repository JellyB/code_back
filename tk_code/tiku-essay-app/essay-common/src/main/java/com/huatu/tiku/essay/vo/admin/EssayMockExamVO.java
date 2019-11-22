package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by x6 on 2017/12/28.
 * 申论模考
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EssayMockExamVO {

    //id(模考试卷和模考id相同)
    private long id;

    /*  bizStatus   0初始化  1 已开始  2 已结束  */
    //模考名称11
    private String name;

    //平均分
    private double avgScore;
    //最高分
    private double maxScore;
    //报名总人数
    private double enrollCount;
    //考试总人数
    private double examCount;

    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;

    //答题限时
    private int limitTime;
    //年份
    private String paperYear;
    private String paperDate;
    //试卷总分
    private double score;

    //解析课介绍
    private String courseInfo;
    //解析课id
    private int courseId;
    //考试说明
    private String instruction;
    private String instructionPC;


    //tag
    private int tag;
    //是否是联合模考(1 联合模考 2申论模考)
    private int mockType;



}
