package com.huatu.springboot.webMessage.core;

import com.huatu.tiku.common.consts.RabbitConsts;

/**
 * 数据上报 队列名称
 * Created by junli on 2018/4/2.
 */
public enum RabbitMqReportQueueEnum {
    QUEUE_REPORT(RabbitConsts.QUEUE_REPORT, false),//数据上报队列名称
    QUEUE_GALAXY_SEARCH(RabbitConsts.QUEUE_GALAXY_SEARCH, true);//云 数据统计队列名称

    private String queueName;

    /**
     * 该队列是否上报简单数据 - if true 只上报 extraData 对象
     */
    private boolean simpleData;


    RabbitMqReportQueueEnum(String queueName, boolean simpleData) {
        this.queueName = queueName;
        this.simpleData = simpleData;
    }

    public String getQueueName() {
        return queueName;
    }

    public boolean isSimpleData() {
        return simpleData;
    }
}
