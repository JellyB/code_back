package com.huatu.ztk.paper.common;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * 模考大赛错误
 * Created by linkang on 2017/10/02 下午3:39
 */
public class MatchErrors {


    /**
     * 查找不到当前的考试科目下面的模考大赛
     */
    public static final ErrorResult NO_MATCH = ErrorResult.create(10031006, "尚无模考大赛");
    /**
     * 考试开始30分钟后，无法报名，无法创建答题卡
     */
    public static final ErrorResult MISSING_MATCH = ErrorResult.create(10031007, "已错过模考大赛");

    /**
     * 未报名的模考大赛不能参加
     */
    public static final ErrorResult NOT_ENROLL = ErrorResult.create(10031008, "模考大赛未报名");

    /**
     * 模考大赛未开始
     */
    public static final ErrorResult NOT_START = ErrorResult.create(10031009, "模考大赛考试未开始");

}
