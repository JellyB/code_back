package com.huatu.sensors.entity.essay;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 申论模考
 * @author zhangchong
 *
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_mock_exam")
public class EssayMockExam {


    @Id
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.IDENTITY)
    protected Long id;
    protected Integer bizStatus;
    protected Integer status;
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
    private Integer enrollCount;
    //考试总人数
    private Integer examCount;

    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;

    //行测id
    private Integer practiceId;
    //年份
    private String paperYear;


    //tag
    private Integer tag;
    //是否是联合模考(1 联合模考 2申论模考)
    private Integer mockType;
    //解析课介绍
    private String courseInfo;
    //解析课id
    private Integer courseId;
    //考试说明
    private String instruction;
    @Column(name="instructionPC")
    private String instructionPC;


}
