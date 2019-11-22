package com.huatu.tiku.essay.constant.status;

/**
 * Created by linkang on 17-7-14.
 */
public class MatchStatus {
    /**
     * 未报名
     */
    public static final int UN_ENROLL = 1;

    /**
     * 未报名且错过报名
     */
    public static final int PASS_UP_ENROLL = 9;

    /**
     * 已报名
     */
    public static final int ENROLL = 2;

    /**
     * 开始考试-置灰-不可用
     */
    public static final int START_UNAVILABLE = 3;

    /**
     * 开始考试
     */
    public static final int START_AVILABLE = 4;

    /**
     * 无法考试
     */
    public static final int MATCH_UNAVILABLE = 5;

    /**
     * 可查看报告
     */
    public static final int REPORT_AVAILABLE = 6;

    /**
     * 未出报告
     */
    public static final int REPORT_UNAVILABLE = 7;

    /**
     * 未交卷，可以继续做题
     */
    public static final int NOT_SUBMIT = 8;

}
