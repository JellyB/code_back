package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.VideoFeedback;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangjian
 **/
public interface VideoFeedbackRepository extends BaseRepository<VideoFeedback, Long> {

    /**
     * 根据年月查找
     */
    VideoFeedback findByCourseIdAndYearAndMonthAndFeedbackStatusIn(Long courseId,Integer year,Integer month,List<FeedbackStatus> list);

    /**
     * 批量审核
     */
    @Transactional
    @Modifying
    @Query("update  VideoFeedback  set feedbackStatus =?2 where id in ?1")
    Integer updateStatus(List<Long> ids, FeedbackStatus feedbackStatus);
}
