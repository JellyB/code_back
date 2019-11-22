package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.VideoFeedback;
import com.huatu.tiku.schedule.biz.domain.VideoFeedbackInfo;
import com.huatu.tiku.schedule.biz.dto.VideoFeedbackUpdateDto;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author wangjian
 **/
public interface VideoFeedbackInfoService extends BaseService<VideoFeedbackInfo, Long> {

    /**
     * 详情
     */
    List<VideoFeedbackInfo> findByVideoFeedbackId(Long id);

    /**
     * 分页查找
     */
    Page<VideoFeedback> findVideoFeedbackList(Long courseId, String name, FeedbackStatus status, Pageable page);

    List<Map> importExcel(List<List<List<String>>> list);

    /**
     * 根据录播反馈ID更新信息
     *
     * @param feedbackUpdateDto 更新信息
     */
    void updateFeedback(VideoFeedbackUpdateDto feedbackUpdateDto) throws Exception;
}
