package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by x6 on 2017/12/28.
 * 申论模考
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name="v_essay_mock_exam")
@DynamicUpdate
@DynamicInsert
public class EssayMockExam {


    @Id
    protected Long id;
    @Column(columnDefinition = "smallint default 0")
    protected int bizStatus;
    @Column(columnDefinition = "smallint default 1")
    protected int status;
    @Column(columnDefinition = "varchar(128) default ''")
    protected String creator;
    @Column(columnDefinition = "varchar(128) default ''")
    protected String modifier;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    protected Date gmtCreate;
    @org.hibernate.annotations.UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
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
    //年份
    private String paperYear;


    //tag
    private int tag;
    //是否是联合模考(1 联合模考 2申论模考)
    private int mockType;
    //解析课介绍
    private String courseInfo;
    //解析课id
    private int courseId;
    //考试说明
    private String instruction;
    private String instructionPC;


}
