package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.VideoRoom;

import java.util.List;

/**
 * @author wangjian
 **/
public interface VideoRoomService extends BaseService<VideoRoom, Long> {
    List<VideoRoom> getVideoRoomList();

    void deleteX(Long roomId, String reason);
}
