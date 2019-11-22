package com.huatu.tiku.match.constant;


import com.huatu.common.ErrorResult;

/**
 * Created by huangqingpeng on 2018/10/25.
 *
 * @author huangqingpeng
 */
public final class MatchErrors {

    public static final ErrorResult MATCH_TAG_PARAM_ERROR = ErrorResult.create(2000001, "标签参数非法");
    public static final ErrorResult MATCH_OUT_END_TIME = ErrorResult.create(2000002, "提交答案的时间超出期限");
    public static final ErrorResult NO_PRACTICEID = ErrorResult.create(2000003, "无模考大赛答题卡");
    public static final ErrorResult NO_ENROLLINFO = ErrorResult.create(2000004, "用户未报名");
    public static final ErrorResult UN_FINISHED = ErrorResult.create(2000004, "考试成绩未处理完成");
    public static final ErrorResult UN_SUBMITED = ErrorResult.create(2000004, "答题卡未完成提交");
    public static final ErrorResult NO_CORRECTED = ErrorResult.create(2000004, "答题卡未完成批改");
    public static final ErrorResult MATCH_DATA_ERROR = ErrorResult.create(2000004, "模考大赛数据非法！");
    public static final ErrorResult NO_REPORT = ErrorResult.create(1000009, "用户暂无报告数据");
}
