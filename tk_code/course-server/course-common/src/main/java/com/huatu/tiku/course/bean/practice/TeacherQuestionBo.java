package com.huatu.tiku.course.bean.practice;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2019/2/18
 */
@Data
@Builder
@NoArgsConstructor
public class TeacherQuestionBo {

    /**
     * ID
     */
    private Long id;

    /**
     * PPT 页码
     */
    private Integer pptIndex;

    /**
     * 试题类型
     */
    private Integer type;

    /**
     * 试题类型 - 名称
     */
    private String typeName;

    /**
     * 材料
     */
    private List<String> materialList;

    /**
     * 题干
     */
    private String stem;

    /**
     * 选项
     */
    private List<String> choiceList;

    /**
     * 答案
     */
    private String answer;

    /**
     * 解析
     */
    private String analysis;

    /**
     * 扩展
     */
    private String extend;

    /**
     * 来源
     */
    private String from;

    /**
     * 开始练习时间
     */
    private Long startPracticeTime;

    /**
     * 剩余练习时间
     */
    private Integer lastPracticeTime;

    /**
     * 练习时长
     */
    private Integer practiceTime;

    /**
     * 知识点信息
     */
    private String pointName;

    @Builder
    public TeacherQuestionBo(Long id, Integer pptIndex, Integer type, String typeName, List<String> materialList, String stem, List<String> choiceList, String answer, String analysis, String extend, String from, Long startPracticeTime, Integer lastPracticeTime, Integer practiceTime, String pointName) {
        this.id = id;
        this.pptIndex = pptIndex;
        this.type = type;
        this.typeName = typeName;
        this.materialList = materialList;
        this.stem = stem;
        this.choiceList = choiceList;
        this.answer = answer;
        this.analysis = analysis;
        this.extend = extend;
        this.from = from;
        this.startPracticeTime = startPracticeTime;
        this.lastPracticeTime = lastPracticeTime;
        this.practiceTime = practiceTime;
        this.pointName = pointName;
    }
}
