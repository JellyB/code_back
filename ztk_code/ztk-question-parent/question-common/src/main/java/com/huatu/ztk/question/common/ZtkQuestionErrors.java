package com.huatu.ztk.question.common;


import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * 试题错误码
 * Created by shaojieyue
 * Created time 2016-04-26 14:47
 */
public class ZtkQuestionErrors {
    /**
     * 笔记内容过长
     */
    public static final ErrorResult NOTE_TOO_LONG = ErrorResult.create(1200101,"笔记内容过长");
}
