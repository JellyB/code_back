package com.huatu.tiku.entity.question;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.util.List;

/**
 * 属性包questionSimpleInfo所有属性 并额外扩展了属性
 * Created by lijun on 2018/8/10
 */
@Data
@NoArgsConstructor
public class PaperQuestionSimpleInfo {

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
     * 解析
     */
    private String analyze;

    /**
     *扩展
     */
    private String extend;


    /**
     * 真题、模拟题
     */
    private Integer mode;

    /**
     * 真题、模拟题 - 名称
     */
    private String modeName;

    /**
     * 材料
     */
    private List<String> materialContent;

    /**
     * 选项
     */
    private List<String> choices;

    /************************ 以下为 paperQuestion特有属性********************************/
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 模块ID
     */
    private Integer moduleId;

    /**
     * 分数
     */
    private Double score;

    /**
     * 知识点名称
     */
    @Transient
    private List<String> knowledgeName;

    @Transient
    private List<Long> knowledgeIds;

    /**
     * 子题
     */
    private List<PaperQuestionSimpleInfo> children;

    public void setChildren(List<PaperQuestionSimpleInfo> children) {
        this.children = children;
    }

    @Builder
    public PaperQuestionSimpleInfo(Long id, Integer questionType, String questionTypeName, Integer bizStatus, String bizStatusName, Integer availFlag, String availFlagName, Integer missFlag, String missFlagName, String source, String stem, String answer, String analyze, String extend, Integer mode, String modeName, List<String> materialContent, List<String> choices, Integer sort, Integer moduleId, Double score, List<PaperQuestionSimpleInfo> children,List<String> knowledgeName) {
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
        this.analyze = analyze;
        this.extend = extend;
        this.mode = mode;
        this.modeName = modeName;
        this.materialContent = materialContent;
        this.choices = choices;
        this.sort = sort;
        this.moduleId = moduleId;
        this.score = score;
        this.children = children;
        this.knowledgeName=knowledgeName;
    }
}
