package com.huatu.tiku.entity.question;

import com.huatu.common.bean.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 教师类的主试题表
 * Created by huangqp on 2018\4\14 0014.
 */
@Data
@NoArgsConstructor(force = true)
@Table(name = "base_question")
public class BaseQuestion extends BaseEntity {
    /**
     * 真题、模拟题
     */
    private Integer mode;

    /**
     * 试题实际类型
     * 客观类：1单选题2多选题3不定项选择题4选词填空（一边文章多个选项按顺序选择）
     * 判断类：4判断题5辨析题
     * 主观类：6名词解析7简答8论述9教学设计10教育方案设计11作文
     * 连线类：12连线题
     * 复合类：13阅读理解14完形填空15材料分析16案例分析
     */
    private Integer questionType;

    /**
     * 是否是特岗教师(1是0否)（源于试卷属性）
     */
    private Integer specialFlag;

    /**
     * 分数（暂时不用）
     */
    private Double score;

    /**
     * 复合题Id
     */
    private Long multiId;

    /**
     * 0全部1识记类2理解类
     */
    private Integer inspectField;

    /**
     * 是否是复合题
     */
    private Integer multiFlag;

    /**
     * 是否是有用题(1有用2废弃)
     */
    private Integer availFlag;

    /**
     * 缺失题标识（1缺失2正常）
     */
    private Integer missFlag;

    /**
     * 难度
     */
    private Integer difficultyLevel;

    /**
     * 科目
     */
    private Long subjectId;

    /**
     * 是否全量同步
     */
    private Integer copyFlag;

    /**
     * 审核人
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private Timestamp gmtAudit;
    /**
     * 统计标签,不参与业务，只为后期大数据试题统计使用
     */
    private String statisticsTag;


    @Builder
    public BaseQuestion(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, Integer mode, Integer questionType, Integer specialFlag, Double score, Long multiId, Integer inspectField, Integer multiFlag, Integer availFlag, Integer missFlag, Integer difficultyLevel, Long subjectId, Integer copyFlag, Long auditorId, Timestamp gmtAudit) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.mode = mode;
        this.questionType = questionType;
        this.specialFlag = specialFlag;
        this.score = score;
        this.multiId = multiId;
        this.inspectField = inspectField;
        this.multiFlag = multiFlag;
        this.availFlag = availFlag;
        this.missFlag = missFlag;
        this.difficultyLevel = difficultyLevel;
        this.subjectId = subjectId;
        this.copyFlag = copyFlag;
        this.auditorId = auditorId;
        this.gmtAudit = gmtAudit;
    }
}
