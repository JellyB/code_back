package com.huatu.tiku.course.common;

/**
 * @author shanjigang
 * @date 2019/2/27 16:46
 */
public class PeriodTestStatus {
    /**
     * 未开始
     */
    public static final int NOT_START = 1;

    /**
     *正在进行
     */
    public static final int ONLINE = 2;

    /**
     *已经结束
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

}
