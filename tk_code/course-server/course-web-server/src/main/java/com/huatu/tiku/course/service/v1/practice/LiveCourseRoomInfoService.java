package com.huatu.tiku.course.service.v1.practice;

import java.util.List;

/**
 * Created by lijun on 2019/2/21
 */
public interface LiveCourseRoomInfoService {

    /**
     * 根据RoomId 获取课件数据信息
     */
    List<Integer> getLiveCourseIdListByRoomId(Long roomId);
}
