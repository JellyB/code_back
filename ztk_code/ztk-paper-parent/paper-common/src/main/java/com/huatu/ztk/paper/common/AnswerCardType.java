package com.huatu.ztk.paper.common;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 答题卡类型
 * Created by shaojieyue
 * Created time 2016-05-03 11:44
 */
public class AnswerCardType {
    /**
     *
     */
    public static final int ANY_PAPER = 0;

    /**
     * 智能出题 快速练习
     */
    public static final int SMART_PAPER = 1;

    /**
     * 专项练习
     */
    public static final int CUSTOMIZE_PAPER = 2;

    /**
     * 真题
     */
    public static final int TRUE_PAPER = 3;

    /**
     * 模拟题,旧版的精准估分type
     */
    public static final int MOCK_PAPER = 4;


    /**
     * 竞技练习
     */
    public static final int ARENA_PAPER = 5;

    /**
     * 错题练习
     */
    public static final int WRONG_PAPER = 6;

    /**
     * 每日训练
     */
    public static final int DAY_TRAIN = 7;

    /**
     * 收藏练习
     */
    public static final int COLLECT_TRAIN = 8;

    /**
     * 模考
     */
    public static final int SIMULATE = 9;

    /**
     * 砖超
     */
    public static final int LEAGUE_MATCH = 10;

    /**
     * 微信答题
     */
    public static final int WEI_XIN = 11;

    /**
     * 模考大赛
     */
    public static final int MATCH = 12;

    /**
     * 估分
     */
    public static final int ESTIMATE = 13;

    /**
     * 往期模考
     */
    public static final int MATCH_AFTER = 14;

    /**
     * 课程 课后练习
     */
    public static final int COURSE_EXERCISE = 15;

    /**
     * 课程 课中练习
     */
    public static final int COURSE_BREAKPOINT = 16;


    /**
     * 小模考（一天一次，不报名，开始考试不会暂停，交卷后立即查看结果，排名实时更新）
     */
    public static final int SMALL_ESTIMATE = 17;

    /**
     * 阶段测试
     */
    public static final int FORMATIVE_TEST_ESTIMATE = 18;

    /**
     * 小程序试卷类型
     */
    public static final int APPLETS_PAPER = 19;

    /**
     * 错题下载
     */
    public static final int WRONG_PAPER_DOWNLOAD = 20;

    /**
     * 专项训练背题模式
     */
    public static final int CUSTOMIZE_PAPER_RECITE = 21;

    /**
     * 错题重练背题模式
     */
    public static final int WRONG_PAPER_RECITE = 22;



    public static final Map<Integer, String> types = new HashMap<Integer, String>();

    static {
        types.put(1, "智能刷题");
        types.put(2, "专项练习");
        types.put(3, "真题演练");
        types.put(4, "模拟题");
        types.put(5, "竞技练习");
        types.put(6, "错题练习");
        types.put(7, "每日特训");
        types.put(8, "收藏练习");
        types.put(9, "专项模考");
        types.put(10, "砖超");
        types.put(11, "微信答题");
        types.put(12, "模考大赛");
        types.put(13, "精准估分");
        types.put(14, "往期模考");
//        types.put(15,"课后练习");
//        types.put(16,"课中练习");
    }


    public static final Map<Integer, String> recordType = new HashMap<Integer, String>();

    static {
        recordType.put(1, "智能刷题");
        recordType.put(2, "专项练习");
        recordType.put(3, "真题演练");
        recordType.put(4, "模拟题");
        recordType.put(5, "竞技赛场");
        recordType.put(6, "错题重练");
        recordType.put(7, "每日特训");
        recordType.put(8, "收藏练习");
        recordType.put(9, "专项模考");
        recordType.put(10, "砖超");
        recordType.put(11, "微信答题");
        recordType.put(12, "模考大赛");
        recordType.put(13, "精准估分");
        recordType.put(14, "往期模考");
        recordType.put(20, "错题下载");
        recordType.put(21, "专项练习（背题模式）");
        recordType.put(22, "错题重练（背题模式）");
//        recordType.put(15,"课后练习");
//        recordType.put(16,"课中练习");
    }

    /**
     * 获取练习类型
     *
     * @param type
     * @return
     */
    public static final String getTypeName(int type) {
        return Optional.ofNullable(types.get(type)).orElse("");
    }

    /**
     * @creator huangqp
     * 答题记录类型下拉列表规则维护
     * 以招警的考试类型为默认值，且除了公务员科目，其他的都去掉智能刷题功能
     */
    //全量类型
    public static final Integer[] SELECT_ALL = {ANY_PAPER, SMART_PAPER, DAY_TRAIN, CUSTOMIZE_PAPER, WRONG_PAPER, ARENA_PAPER, SIMULATE, TRUE_PAPER, ESTIMATE, MATCH_AFTER,WRONG_PAPER_DOWNLOAD};
    //公务员
    public static final Integer[] SELECT_GWX = {ANY_PAPER, SMART_PAPER, DAY_TRAIN, CUSTOMIZE_PAPER, WRONG_PAPER, TRUE_PAPER, MATCH_AFTER,WRONG_PAPER_DOWNLOAD};
    //事业单位职测
    public static final Integer[] SELECT_SYDW_ZC = {ANY_PAPER, SMART_PAPER, DAY_TRAIN, CUSTOMIZE_PAPER, WRONG_PAPER, TRUE_PAPER, MATCH_AFTER,WRONG_PAPER_DOWNLOAD};
    //事业单位公基
    public static final Integer[] SELECT_SYDW = {ANY_PAPER, DAY_TRAIN, CUSTOMIZE_PAPER, WRONG_PAPER, TRUE_PAPER, MATCH_AFTER,WRONG_PAPER_DOWNLOAD};
    //默认类型(招警)
    public static final Integer[] SELECT_DEF = {ANY_PAPER, DAY_TRAIN, CUSTOMIZE_PAPER, WRONG_PAPER, TRUE_PAPER,WRONG_PAPER_DOWNLOAD};

    public static final String getSelectName(int type) {
        if (0 == type) {
            return "类型不限";
        }
        return Optional.ofNullable(recordType.get(type)).orElse("");
    }
}
