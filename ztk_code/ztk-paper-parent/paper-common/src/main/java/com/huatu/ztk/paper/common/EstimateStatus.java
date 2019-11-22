package com.huatu.ztk.paper.common;

/**
 * 模考估分试卷的状态
 * Created by linkang on 7/25/16.
 */
public class EstimateStatus {
    /**
     * 未开始
     */
    public static final int NOT_START = 1;

    /**
     *正在进行
     */
    public static final int ONLINE = 2;

    /**
     *已经结束（阶段测试没有结束状态，超过结束时间还可以答题）
     */
    public static final int END = 3;

    /**
     * 已下线
     */
    public static final int OFFLINE = 4;

    /**
     *可继续做题
     */
    public static final int CONTINUE_AVAILABLE = 5;

    /**
     * 可查看报告
     */
    public static final int REPORT_AVAILABLE = 6;

    /**
     * 未出报告
     */
    public static final int REPORT_UNAVILABLE = 7;

}
