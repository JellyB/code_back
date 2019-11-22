package com.huatu.tiku.course.spring.conf.queue;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-01 11:36 AM
 **/
@NoArgsConstructor
@Getter
@Setter
public class Payload{
    private int syllabusId;
    private String userName;

    @Builder
    public Payload(int syllabusId, String userName) {
        this.syllabusId = syllabusId;
        this.userName = userName;
    }
}
