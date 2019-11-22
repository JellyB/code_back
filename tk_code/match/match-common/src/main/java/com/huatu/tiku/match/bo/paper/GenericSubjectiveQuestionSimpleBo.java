package com.huatu.tiku.match.bo.paper;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2019/2/15
 */
@Data
@NoArgsConstructor
public class GenericSubjectiveQuestionSimpleBo extends QuestionSimpleBo {

    /**
     * 题目要求
     */
    private String require;

    /**
     * 答题要求
     */
    private String answerRequire;

    /**
     * 最大字数
     */
    private int maxWordCount;

    /**
     * 最小字数
     */
    private int minWordCount;

    @Builder
    public GenericSubjectiveQuestionSimpleBo(Integer id, Integer parentId, Integer type, String typeName, List<String> materialList, String stem, String moduleName, String require, String answerRequire, int maxWordCount, int minWordCount) {
        super(id, parentId, type, typeName, materialList, stem, moduleName);
        this.require = require;
        this.answerRequire = answerRequire;
        this.maxWordCount = maxWordCount;
        this.minWordCount = minWordCount;
    }
}
