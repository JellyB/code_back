package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "paper_activity")
public class PaperActivity extends BaseEntity {
    /**
     * 试卷id
     */
    private Long paperId;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 活动类型（1真题2万人模考3作业吧4定期模考8估分试卷9模考大赛14往期模考）
     */
    private Integer type;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 试卷分数
     */
    @Column(name = "score")
    private Double totalScore;
    /**
     * 答题时限
     */
    private Integer limitTime;
    /**
     * 地区
     */
    private List<Long> areaIds;
    /**
     * 查看报告方式
     */
    private Integer lookParseTime;
    /**
     * 是否隐藏
     */
    private Integer hideFlag;
    /**
     * 关联课程id
     */
    private Long courseId;
    /**
     * 关联课程描述
     */
    private String courseInfo;
    /**
     * 考试说明
     */
    private String instruction;
    /**
     * pc端考试说明
     */
    @Column(name = "instruction_pc")
    private String instructionPC;
    /**
     * 活动标签
     */
    private Integer tag;
    /**
     * 申论考试id
     */
    private Long essayId;
    /**
     * 申论考试开始时间
     */
    private Timestamp essayStartTime;
    /**
     * 申论开始结束时间
     */
    private Timestamp essayEndTime;
    /**
     * 上线时间
     */
    private Timestamp onlineTime;
    /**
     * 下线时间
     */
    private Timestamp offlineTime;
    /**
     * 开始时间
     */
    private Timestamp startTime;
    /**
     * 结束时间
     */
    private Timestamp endTime;

    /**
     * 模块信息
     */
    private String module;

    /**
     * 活动开始时间
     */
    private Timestamp activityStartTime;

    /**
     * 活动结束时间
     */
    private Timestamp activityEndTime;

    /**
     * 科目ID
     */
    @Transient
    private List<Long> subjectIds;

    @Transient
    private String areaNames;

    @Transient
    private String activityTime;

    @Transient
    private String examTime;

    @Transient
    private Map activityStatus;

    /**
     * 课程名称（小模考使用）
     */
    private String courseName;
    /**
     * 在配置的考试时间内,是否生效(阶段测试使用)
     * 0 否;1 是
     */
    private Integer startTimeIsEffective;

    /**
     * 试卷计算分数时,是否按照老师输入的分数计算
     * （1按照老师设置分数计算;0按照正确率计算）
     */
    private Integer scoreFlag;


    /**
     * 试题来源是否计入（0不计入1计入）
     */
    private Integer sourceFlag;

    /**
     * 试卷内试题分数总和
     */
    @Transient
    private Double questionTotalScore = 0D;
    
    /**
     * 小程序二维码
     */
    private String qrcode;


}

