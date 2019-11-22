package com.huatu.tiku.match.constant;

/**
 * 模考大赛消息队列
 * Created by huangqingpeng on 2018/8/25.
 */
public class RabbitMatchKeyConstant {
    /**
     * 模考大赛旧模考用户考试报名及成绩同步
     */
    public final static String MATCH_USER_META_SYNC = "match_user_meta_sync";
    /**
     * 模考大赛旧模考试题统计信息同步
     */
    public final static String MATCH_QUESTION_META_SYNC = "match_question_meta_sync";
    /**
     * 异步处理模考大赛答题卡交卷信息
     */
    public final static String ANSWER_CARD_SUBMIT_ASYNC = "answer_card_submit_async";

    /**
     * 用户提交的答案
     */
    public static final String SUBMIT_ANSWERS = "submit_answers";

    /**
     * 用户答案数据发送给数据中心
     */
    public static final String RABBIT_TOPIC_RECORD_QUEUE_NAME = "spark_topic_record";

}
