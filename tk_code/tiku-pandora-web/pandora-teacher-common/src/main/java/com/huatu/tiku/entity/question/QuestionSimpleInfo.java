package com.huatu.tiku.entity.question;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.util.List;

/**
 * Created by lijun on 2018/8/9
 */
@Data
@NoArgsConstructor
public class QuestionSimpleInfo {
    /**
     * id
     */
    private Long id;

    /**
     * 试题类型
     */
    private Integer questionType;

    /**
     * 试题类型对应的名称
     */
    private String questionTypeName;

    /**
     * 试题状态
     */
    private Integer bizStatus;

    /**
     * 试题状态 -名称
     */
    private String bizStatusName;

    /**
     * 作废(1有用2废弃)
     */
    private Integer availFlag;

    /**
     * 作废 - 名称
     */
    private String availFlagName;

    /**
     * 残缺
     */
    private Integer missFlag;

    /**
     * 残缺 - 名称
     */
    private String missFlagName;

    /**
     * 来源
     */
    private String source;

    /**
     * 题目
     */
    private String stem;

    /**
     * 答案
     */
    private String answer;

    /**
     * 真题、模拟题
     */
    private Integer mode;

    /**
     * 真题、模拟题 - 名称
     */
    private String modeName;

    /**
     * 解析
     */
    private String analyze;

    /**
     * 扩展
     */
    private String extend;

    /**
     * 材料
     */
    private List<String> materialContent;

    /**
     * 选项
     */
    private List<String> choices;

    /**
     * 子题
     */
    private List<QuestionSimpleInfo> children;

    /**
     * 知识点名称
     */
    @Transient
    private List<String> knowledgeName;

    @Transient
    private List<Long> knowledgeIds;

    /**
     * 手工组卷方便排序,使用此字段,别的地方不需要
     */
    @Transient
    private Integer sort;

    @Transient
    private Integer status;


    @Builder
    public QuestionSimpleInfo(Long id, Integer questionType, String questionTypeName, Integer bizStatus, String bizStatusName, Integer availFlag, String availFlagName, Integer missFlag, String missFlagName, String source, String stem, String answer, Integer mode, String modeName, String analyze, String extend, List<String> materialContent, List<String> choices, List<QuestionSimpleInfo> children, List<String> knowledgeName, List<Long> knowledgeIds, Integer sort, Integer status) {
        this.id = id;
        this.questionType = questionType;
        this.questionTypeName = questionTypeName;
        this.bizStatus = bizStatus;
        this.bizStatusName = bizStatusName;
        this.availFlag = availFlag;
        this.availFlagName = availFlagName;
        this.missFlag = missFlag;
        this.missFlagName = missFlagName;
        this.source = source;
        this.stem = stem;
        this.answer = answer;
        this.mode = mode;
        this.modeName = modeName;
        this.analyze = analyze;
        this.extend = extend;
        this.materialContent = materialContent;
        this.choices = choices;
        this.children = children;
        this.knowledgeName = knowledgeName;
        this.knowledgeIds = knowledgeIds;
        this.sort = sort;
        this.status = status;
    }
}
