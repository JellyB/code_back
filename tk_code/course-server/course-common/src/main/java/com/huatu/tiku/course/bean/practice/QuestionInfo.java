package com.huatu.tiku.course.bean.practice;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2019/2/21
 */
@Data
@NoArgsConstructor
@Builder
public class QuestionInfo {

    private Long id;

    /**
     * 类型
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
     * 知识点信息
     */
    private String pointName;

    @Builder
    public QuestionInfo(Long id, Integer type, String typeName, List<String> materialList, String stem, List<String> choiceList, String answer, String analysis, String extend, String from, String pointName) {
        this.id = id;
        this.type = type;
        this.typeName = typeName;
        this.materialList = materialList;
        this.stem = stem;
        this.choiceList = choiceList;
        this.answer = answer;
        this.analysis = analysis;
        this.extend = extend;
        this.from = from;
        this.pointName = pointName;
    }
}
