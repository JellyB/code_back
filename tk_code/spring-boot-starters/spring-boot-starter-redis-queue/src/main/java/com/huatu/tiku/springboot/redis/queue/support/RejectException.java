package com.huatu.tiku.springboot.redis.queue.support;

/**
 * @author hanchao
 * @date 2017/11/1 10:48
 */
public class RejectException extends RuntimeException {
    public RejectException(String msg){
        super(msg);
    }
}
