package com.huatu.tiku.schedule.biz.repository;


import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.VideoFeedbackInfo;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
public interface VideoFeedbackInfoRepository extends BaseRepository<VideoFeedbackInfo, Long> {
    List<VideoFeedbackInfo> findByVideoFeedbackId(Long id);

    @Query(nativeQuery = true,value = "SELECT i.* FROM video_feedback_info i LEFT JOIN video_feedback f ON f.id = i.video_feedback_id WHERE i.teacher_id = ?3 AND f.date >= ?1 AND f.date <= ?2 AND f.feedback_status = 2")
    List<VideoFeedbackInfo> findByDateAndTeacherId(Date dateBegin, Date dateEnd, Long teacherId);
}
