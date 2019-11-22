package com.huatu.tiku.interview.constant.cache;

import com.google.common.base.Joiner;

/**
 * Created by x6 on 2018/1/18.
 */
public class RedisKeyConstant {

    /*  生成报告定时任务锁  */
    public static String SAVE_REPORT_LOCK = "save_report_lock";

    /*  获取token定时任务锁  */
    public static String GET_TOKEN_LOCK = "get_token_lock";

    public static String PUSH_NOTIFICATION_LOCK = "get_notify_lock";

    public static String SAVE_Notification_LOCK="save_notify_lock";


    /* 练习内容 */
    public static String PRACTICE_CONTENT_TYPE = "practice_content_type";

    /* 练习内容 点评数据 （优点&问题）*/
    public static String PRACTICE_CONTENT_REMARK = "practice_content_remark";

    /**
     * 练习内容 点评数据 （优点&问题）
     * @param typeId
     * @return
     */
    public static String  getPracticeContentTypeKey(long typeId) {
        return Joiner.on("_").join(RedisKeyConstant.PRACTICE_CONTENT_REMARK,typeId);
    }
    /* 综合评价 词库*/
    public static String REMARK_WORD = "remark_word";


    /* 表现*/
    public static String EXPRESSION = "expression";

    /* 全真模考试卷列表 */
    public static String MOCK_PAPER_LIST = "mock_paper";



}
