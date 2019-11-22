package com.huatu.tiku.course.spring.conf.queue;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-26 下午2:46
 **/
public interface DelayQueueProcessListener {

    void ackCallback(Message message);

    void peekCallback(Message message);

    void pushCallback(Message message);

}
