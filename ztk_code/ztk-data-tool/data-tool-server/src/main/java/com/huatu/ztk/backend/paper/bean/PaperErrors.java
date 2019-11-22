package com.huatu.ztk.backend.paper.bean;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by linkang on 2/17/17.
 */
public class PaperErrors {

    public static final ErrorResult EXISTS_ESTIMATE_PAPER = ErrorResult.create(12001, "已经存在同名的估分试卷");


    public static final ErrorResult EXISTS_MODULE = ErrorResult.create(12002, "该模块已经存在");


    public static final ErrorResult NO_USER_RESULT = ErrorResult.create(12003, "尚无用户答题数据");

    public static final ErrorResult EXISTS_QID = ErrorResult.create(12004, "该试卷已经存在此试题id");

    public static final ErrorResult RECOMMEND_UNAVAILABLE = ErrorResult.create(12005, "该试卷未处于上线状态,无法添加到推荐");

    public static final ErrorResult RECOMMEND_ALREADY = ErrorResult.create(12006, "该试卷已添加到推荐");
}
