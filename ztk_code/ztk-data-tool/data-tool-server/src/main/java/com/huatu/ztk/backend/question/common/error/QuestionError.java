package com.huatu.ztk.backend.question.common.error;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-21  14:19 .
 */
public class QuestionError {
    public static final ErrorResult EDIT_ALEADY_EXIT = ErrorResult.create(1100001,"该题存在未审核的修改，不能再进行修改！");
    public static final ErrorResult EDIT_FAIL = ErrorResult.create(1100002,"修改该题出现异常");
    public static final ErrorResult EDIT_ALEADY_REVIEW = ErrorResult.create(1100003,"试题修改已由他人进行审核了！");
    public static final ErrorResult SEQUENCE_ALEADY_EXIT = ErrorResult.create(1100004,"题序已经存在！");
    public static final ErrorResult REVIEW_FAIL = ErrorResult.create(1100005,"审核失败！");
}
