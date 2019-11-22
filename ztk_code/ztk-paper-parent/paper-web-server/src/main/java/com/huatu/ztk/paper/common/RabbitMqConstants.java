package com.huatu.ztk.paper.common;

/**
 * rebbitmq exchange 和 queue声明
 * Created by shaojieyue
 * Created time 2016-06-01 11:32
 */
public class RabbitMqConstants {

    /**
     * 提交试卷的消息队列
     */
    public static final String SUBMIT_PRACTICE_EXCHANGE = "submit_practice_exchange";

    /**
     * 用户提交的答案
     */
    public static final String SUBMIT_ANSWERS = "submit_answers";

    /**
     * 用户答案数据发送给数据中心
     */
    @Deprecated
    public static final String RABBIT_TOPIC_RECORD_QUEUE_NAME = "spark_topic_record";
    
    /**
     *阶段测试交卷发送给course上报给php
     */
    public static final String PERIOD_TEST_SUBMIT_CARD_INFO = "period_test_submit_card_info";
    
    /**
     * 录播随堂练数据统计
     */
    public static final String COURSE_BREAKPOINT_PRACTICE_SAVE_DB_QUEUE = "course_breakpoint_practice_save_db_queue";
    
    
}
