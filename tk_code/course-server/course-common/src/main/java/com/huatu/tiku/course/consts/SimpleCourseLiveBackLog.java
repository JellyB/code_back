package com.huatu.tiku.course.consts;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-10 3:38 PM
 **/

@Getter
@Setter
@NoArgsConstructor
public class SimpleCourseLiveBackLog {

    private Long roomId;
    private Long liveCourseWareId;
    private Long liveBackCourseWareId;

    @Builder
    public SimpleCourseLiveBackLog(Long roomId, Long liveCourseWareId, Long liveBackCourseWareId) {
        this.roomId = roomId;
        this.liveCourseWareId = liveCourseWareId;
        this.liveBackCourseWareId = liveBackCourseWareId;
    }
}
