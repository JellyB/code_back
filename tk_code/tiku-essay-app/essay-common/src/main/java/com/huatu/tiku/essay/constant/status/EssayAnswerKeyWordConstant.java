package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\12\7 0007.
 */
public class EssayAnswerKeyWordConstant {
    /**
     * 试题下关键词（关联试题id）
     */
    public static final int QUESTION_PARENT_KEYWORD_WITHOUT_DESC = 1;
    /**
     * 试题下近义词（关联其他关键词的id）
     */
    public static final int QUESTION_KEYWORD_CHILD_KEYWORD = 2;
    /**
     * 试题下的关键句的关键词（关联关键句id）
     */
    public static final int QUESTION_KEYPHRASE_CHILD_KEYWORD = 3;
    /**
     * 试题下格式规则中标题绑定的关键词（关联格式id，指向格式下的标题）
     */
    public static final int QUESTION_FROM_TITLE_CHILD_KEYWORD = 4;
    /**
     * 试题下格式规则中称呼绑定的关键词（关联格式id,指向格式下的称呼）
     */
    public static final int QUESTION_FROM_APPELLATION_CHILD_KEYWORD = 5;
    /**
     * 试题下格式规则中落款绑定的关键词（关联格式id,指向落款下的称呼）
     */
    public static final int QUESTION_FROM_INSCRIBE_CHILD_KEYWORD = 6;

    /**
     * 试题下关键词,有描述（关联描述id）
     */
    public static final int QUESTION_PARENT_KEYWORD_WITH_DESC = 7;
}
