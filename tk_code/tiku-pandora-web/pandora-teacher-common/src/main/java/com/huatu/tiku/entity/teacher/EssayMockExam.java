package com.huatu.tiku.entity.teacher;

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
public class EssayMockExam {


    protected long id;
    protected int bizStatus;
    protected int status;
    protected String creator;
    protected String modifier;
    protected Date gmtCreate;
    protected Date gmtModify;

    /*  bizStatus   0初始化  1已关联 2已上线  3已结束  */
    //模考名称
    private String name;

    //平均分
    private double avgScore;
    //最高分
    private double maxScore;
    //报名总人数
    private int enrollCount;
    //考试总人数
    private int examCount;

    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;

    //行测id
    private long practiceId;


}
