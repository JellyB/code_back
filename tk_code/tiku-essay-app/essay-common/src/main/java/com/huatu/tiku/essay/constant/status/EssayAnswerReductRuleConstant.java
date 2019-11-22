package com.huatu.tiku.essay.constant.status;

/**
 * Created by huangqp on 2017\12\12 0012.
 */
public class EssayAnswerReductRuleConstant {
    //1为字数限制，2为句子冗余,3为分段规则，4为严重分条,5为特殊分条
    //字数限制
    public final static int WORDNUM_LIMIT = 1;
    //句子冗余
    public final static int REDUNDANT_SENTENCE_LIMIT = 2;
    //分段分条
    public final static int STRIP_SEGMENTAL_RANGE = 3;
    //严重分条(标准分条)
    public final static int NORMAL_STRIP_RANGE = 4;
    //抄袭度匹配
    public final static int DEGREE_OF_PLAGIARISM = 5;
    //特殊分条
    public final static int SPECIAL_STRIP_RANGE = 6;

}
