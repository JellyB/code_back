package com.huatu.tiku.interview.constant;

import com.huatu.common.ErrorResult;

/**
 *  面试服务相关报错
 * Created by huangqp on 2017\12\3 0003.
 */
public class InterviewErrors {


    public static final ErrorResult USER_NOT_EXIST = ErrorResult.create(1000501, "用户不存在");

    public static final ErrorResult PASSWORD_ERROR = ErrorResult.create(1000502, "用户密码错误");

    public static final ErrorResult PAPER_NOT_EXIST = ErrorResult.create(1000503, "试卷id错误，试卷不存在");

    public static final ErrorResult ANSWER_DATE_NOT_EXIST = ErrorResult.create(1000503, "参数异常，答题日期不存在");



}

