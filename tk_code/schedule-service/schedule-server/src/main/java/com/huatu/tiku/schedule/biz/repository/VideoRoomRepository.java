package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.VideoRoom;

import java.util.List;

/**
 * @author wangjian
 **/
public interface VideoRoomRepository extends BaseRepository<VideoRoom, Long> {
    List<VideoRoom> findByShowFlagIsTrue();
}
