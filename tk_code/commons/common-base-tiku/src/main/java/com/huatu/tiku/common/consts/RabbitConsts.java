package com.huatu.tiku.common.consts;

/**
 * @author hanchao
 * @date 2017/10/31 10:16
 */
public class RabbitConsts {
    public static final String ARG_DLX = "x-dead-letter-exchange";
    public static final String ARG_DLK = "x-dead-letter-routing-key";


    public static final String DLX_DEFAULT = "dead_letter_exchange";
    public static final String DLQ_DEFAULT = "dead_letter_queue_default";
    public static final String DLK_DEFAULT = "dead_letter_route_default";


    /**
     * 数据上报队列
     */
    public static final String QUEUE_REPORT = "data_report";

    /**
     * 云 数据统计队列名称
     */
    public static final String QUEUE_GALAXY_SEARCH = "galaxy_data_active";

    /**
     * 免费课赠送
     */
    @Deprecated
    public static final String QUEUE_SEND_FREE_COURSE = "send_free_course_queue";
    /**
     * 用户昵称更新
     */
    public static final String QUEUE_USER_NICK_UPDATE = "user_nick_update_queue";
    /**
     * 每日任务
     */
    public static final String QUEUE_REWARD_ACTION = "reward_action_queue";
}
