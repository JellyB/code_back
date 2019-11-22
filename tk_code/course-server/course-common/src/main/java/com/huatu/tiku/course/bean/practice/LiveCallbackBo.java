package com.huatu.tiku.course.bean.practice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lijun on 2019/3/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveCallbackBo {

    /**
     * 直播ID
     */
    private Long liveCourseId;

    /**
     * 回放ID
     */
    private Long recordCourseId;
}
