package com.huatu.tiku.course.spring.conf.queue;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-26 下午2:40
 **/
public interface DelayQueue {

    String MESSAGE = "$message";
    String QUEUE = "$queue";
    String UN_ACK = "$unAck";

    /**
     * 消息确认消费 ack
     * @param messageId
     * @return
     */
    boolean ack(String messageId);

    /**
     * 设置消息 unAck 过期时间
     * @param messageId
     * @param timeout
     * @return
     */
    boolean setUnAckTimeout(String messageId, long timeout);

    /**
     * 设置消息过期时间
     * @param messageId
     * @param timeout
     * @return
     */
    boolean setTimeout(String messageId, long timeout);

    /**
     * 获取 message 信息
     * @param messageId
     * @return
     */
    Message get(String messageId);

    /**
     * 队列 size
     * @return
     */
    long size();

    /**
     * 清空队列
     */
    void clear();

    /**
     * 压入一条消息到队列中
     * @param message
     * @return
     */
    boolean push(Message message);

    /**
     * 延时消息定时监听
     */
    void listen();


}
