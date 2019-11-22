package com.huatu.ztk.paper.common;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * @author shanjigang
 * @date 2019/3/1 10:31
 */
public class PeriodTestConstant {
    public static final ErrorResult CANNOT_CREATE_PERIODTEST_ANSWERCARD = ErrorResult.create(5000101, "阶段测试活动还没开始不能创建答题卡");
    public static final ErrorResult SYLLABUSID_ISNOT_NULL = ErrorResult.create(5000102, "大纲Id不能为空");
    public static final ErrorResult PAPERID_ISNOT_EXISTS = ErrorResult.create(5000103, "试卷不存在");
    public static final String PASSFIFTYTYPE = "EXPENDPASS50";
    public static final String DIFFCULTYTYPE="DIFFCULTYPASS50";
    public static final String UNDOTYPE="UNDO";
    public static final String PASSFIFTYVALUE = "用时超过50s";
    public static final String DIFFCULTYVALUE="难度高于0.5";
    public static final String UNDOVALUE="没做的题目";
    public static final long AFTERSEVENDAY= 7*24*3600*1000;
    public static final String LTFIFTY="LESSTHANFIFITY";
    public static final String LTEIGHTY= "BETWEENFIFTYANDEIGHTY";
    public static final String GTEIGHTY= "GREATEREIGHTY";

    PeriodTestConstant(){

    }
}
