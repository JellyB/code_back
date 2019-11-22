package com.huatu.tiku.request.paper;

import com.huatu.tiku.request.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by huangqp on 2018\7\6 0006.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPaperReq extends BaseReq {
    /**
     * 活动名称
     */
    @NotBlank(message = "考试/练习名称不能为空")
    private String name;

    /**
     * 活动类型（1真题2万人模考3作业吧4定期模考8估分试卷9模考大赛14往期模考17小模考18阶段测试）
     */
    @NotNull(message = "试卷类型不能为空")
    private Integer type;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 试卷分数
     */
    private Double totalScore;
    /**
     * 答题时限
     */
    private Integer limitTime;
    /**
     * 地区
     */
    @NotNull
    private List<Long> areaIds;
    /**
     * 科目
     */
    private List<Long> subjectIds;
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
    private String instructionPC;
    /**
     * 活动标签（模考大赛特有）
     */

    private Integer tag;
    /**
     * 申论考试id
     */
    private Long essayId;
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
     * 课程名称（小模考使用）
     */
    private String courseName;
    /**
     * 控制是否在设置的考试时间内生效（阶段测试使用）
     * 0 否;1 是
     */
    private Integer startTimeIsEffective;

    /**
     * 是否按照单题算分标识
     */
    private Integer scoreFlag;

    /**
     * 试卷题源标识
     */
    private Integer sourceFlag;

    /**
     * 试卷内试题分数总和
     */
    @Transient
    private Double questionTotalScore = 0D;


}

