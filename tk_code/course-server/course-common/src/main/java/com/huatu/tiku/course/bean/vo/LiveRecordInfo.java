package com.huatu.tiku.course.bean.vo;

import lombok.*;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-23 11:12 AM
 **/

@NoArgsConstructor
@Getter
@Setter
public class LiveRecordInfo implements Serializable {

    private long syllabusId;
    private long classId;
    private long courseWareId;
    private String bjyRoomId;


    @Builder
    public LiveRecordInfo(long syllabusId, long classId, long courseWareId, String bjyRoomId) {
        this.syllabusId = syllabusId;
        this.classId = classId;
        this.courseWareId = courseWareId;
        this.bjyRoomId = bjyRoomId;
    }
}

