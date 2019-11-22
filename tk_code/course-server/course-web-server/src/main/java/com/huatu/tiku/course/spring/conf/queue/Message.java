package com.huatu.tiku.course.spring.conf.queue;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-26 下午2:45
 **/

@NoArgsConstructor
@Getter
@Setter
public class Message<T> implements Serializable{

    private String id;
    private T payload;
    private long timeout;
    private int priority;
    private long createTime;

    @Builder
    public Message(String id, T payload, long timeout, int priority, long createTime) {
        this.id = id;
        this.payload = payload;
        this.timeout = timeout;
        this.priority = priority;
        this.createTime = createTime;
    }
}
