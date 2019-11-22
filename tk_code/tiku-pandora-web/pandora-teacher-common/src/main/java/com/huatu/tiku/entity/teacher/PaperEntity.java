package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

/**
 * 实体试卷
 * Created by huangqp on 2018\6\23 0023.
 */
@NoArgsConstructor
@Data
@Table(name = "paper_entity")
public class PaperEntity extends BaseEntity {
    /**
     * 试卷名称
     */
    private String name;

    /**
     * 模拟卷or真题卷
     */
    private Integer mode;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 科目id(试题卷只能属于一个科目，不过关联活动卷时，可以是多个科目)
     */
    private Long subjectId;

    /**
     * 试卷时长
     */
    private Integer limitTime;

    /**
     * 总分
     */
    private Double totalScore;

    /**
     * 是否残缺（1正常，2残缺）
     */
    private Integer missFlag;

    /**
     * 考试时间
     */
    private String paperTime;

    /**
     * 是否是特岗教师(1是0否)（源于试卷属性）
     */
    private Integer specialFlag;

    /**
     * 试题来源是否计入（0不计入1计入）
     */
    private Integer sourceFlag;

    /**
     * 模块
     */
    private String module;

    /**
     * 区域ID
     */
    @Transient
    private String areaIds;

    /**
     * 试卷内试题分数总和
     */
    @Transient
    private Double questionTotalScore=0D;

    @Transient
    private List<PaperModuleInfo> moduleInfo;


    @Builder
    public PaperEntity(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String name, Integer mode, Integer year, Long subjectId, Integer limitTime, Double totalScore, Integer missFlag, String paperTime, Integer specialFlag, String module, String areaIds, List<PaperModuleInfo> moduleInfo) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.name = name;
        this.mode = mode;
        this.year = year;
        this.subjectId = subjectId;
        this.limitTime = limitTime;
        this.totalScore = totalScore;
        this.missFlag = missFlag;
        this.paperTime = paperTime;
        this.specialFlag = specialFlag;
        this.module = module;
        this.areaIds = areaIds;
        this.moduleInfo = moduleInfo;
    }
}

