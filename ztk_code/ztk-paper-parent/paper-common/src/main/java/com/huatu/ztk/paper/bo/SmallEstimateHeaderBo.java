package com.huatu.ztk.paper.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小模考首页展示字段管理
 * Created by huangqingpeng on 2019/2/19.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SmallEstimateHeaderBo {
    /**
     * 试卷ID（小模考ID）
     */
    private int paperId;
    /**
     * 小模考名称
     */
    private String name;
    /**
     * 参加人数
     */
    private int joinCount;
    /**
     * 开始时间
     */
    private long startTime;
    /**
     * 结束时间
     */
    private long endTime;
    /**
     * 考察知识点
     */
    private String pointsName;
    /**
     * 试题数量
     */
    private int qcount;
    /**
     * 限定时间（开始考试后，必须在限定时间内结束答题，不可退出，强退后，再次进入，期间计时不停止）
     */
    private int limitTime;
    /**
     * 小模考说明
     */
    private String description;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 课程说明
     */
    private String courseInfo;
    /**
     * 课程ID
     */
    private int courseId;
    /**
     * 状态（借鉴专项模考的状态值2进行中，5继续答题6查看报告）
     */
    private int status;
    /**
     * 练习题Id
     */
    private long practiceId;
    /**
     * 练习题id（PC专用）
     */
    private String idStr;

}
