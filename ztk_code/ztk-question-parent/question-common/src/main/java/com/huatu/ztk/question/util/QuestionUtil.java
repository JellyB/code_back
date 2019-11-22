package com.huatu.ztk.question.util;

/**
 * Created by shaojieyue
 * Created time 2016-04-26 15:17
 */
public class QuestionUtil {
    public static final String[] answers = {"","A","B","C","D","E","F","G","H","I","G","K"};

    /**
     * 给定答案,返回答案名称,例如:1234->ABCD 3->C
     * @param answer
     * @return
     */
    public static final String getAnswerName(int answer){
        final char[] chars = String.valueOf(answer).toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char aChar : chars) {
            stringBuilder.append(answers[Integer.valueOf(aChar+"")]);
        }
        return stringBuilder.toString();
    }
}
