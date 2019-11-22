package com.huatu.tiku.constant;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 判断正则常量
 * Created by huangqp on 2018\7\18 0018.
 */
public class QuestionTailConstant {

    /**
     * 判断选项定位的正则（如果以A开头+非字母数字和汉字的字符，则判断选项内容开始）
     */
    public final static String QUESTION_TO_CHOICE = "^A[^(a-z)(A-Z)(0-9)(\u4e00-\u9fa5)]";

    /**
     * 判断答案定位的正则（【答案】）
     */
    public final static String QUESTION_TO_ANSWER = "^【答案】";

    /**
     * 判断解析定位的正则（【解析】）
     */
    public final static String QUESTION_TO_ANALYSIS = "^【解析】";

    /**
     * 判断拓展定位的正则（【拓展】）
     */
    public final static String QUESTION_TO_EXTEND = "^【拓展】";

    /**
     * 判断标签定位的正则（【标签】）
     */
    public final static String QUESTION_TO_TAG = "^【标签】";

    /**
     * 判断知识点定位的正则（【知识点】）
     */
    public final static String QUESTION_TO_KNOWLEDGE = "^【知识点】";

    /**
     * 判断难度定位的正则（【难度】）
     */
    public final static String QUESTION_TO_DIFFICULT = "^【难度】";

    public static List<String> getTagList() {
        return Lists.newArrayList(QuestionTailConstant.QUESTION_TO_ANSWER, QuestionTailConstant.QUESTION_TO_ANALYSIS,
                QuestionTailConstant.QUESTION_TO_EXTEND, QuestionTailConstant.QUESTION_TO_KNOWLEDGE,
                QuestionTailConstant.QUESTION_TO_TAG, QuestionTailConstant.QUESTION_TO_DIFFICULT);

    }
}

