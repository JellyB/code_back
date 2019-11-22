package com.huatu.ztk.paper.common;


/**
 * 试卷类型
 * Created by shaojieyue
 * Created time 2016-05-09 14:51
 */
public class PaperType {

    /**
     * 真题卷
     */
    public static final int TRUE_PAPER = 1;

    /**
     * 万人模考
     */
    public static final int CUSTOM_PAPER = 2;

    /**
     * 作业试卷,作业吧使用
     */
    public static final int HOMEWORK_PAPER = 3;

    /**
     * 定期模考
     */
    public static final int REGULAR_PAPER = 4;

    /**
     * 估分试卷
     */
    public static final int ESTIMATE_PAPER = 8;


    /**
     * 模考大赛
     */
    public static final int MATCH = 9;

    /**
     * 往期模考(虚拟的试卷类型，数据库中不会出现该类型的试卷，但是如果模考大赛作为往期模考被使用时，他的答题卡类型便是这个)
     */
    public static final int MATCH_AFTER = 14;

    /**
     * 小模考（一天一次，不报名，开始考试不会暂停，交卷后立即查看结果，排名实时更新）
     */
    public static final int SMALL_ESTIMATE = 17;

    /**
     * 阶段测试
     */
    public static final int FORMATIVE_TEST_ESTIMATE = 18;

    /**
     * 小程序试卷
     */
    public static final int APPLETS_PAPER = 19;
}
