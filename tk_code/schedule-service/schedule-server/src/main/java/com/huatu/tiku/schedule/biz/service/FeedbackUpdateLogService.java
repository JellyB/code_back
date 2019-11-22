package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.service.BaseService;
import com.huatu.tiku.schedule.biz.domain.FeedbackUpdateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 课时反馈修改日志ø
 *
 * @author geek-s
 * @date 2019-04-28
 */
public interface FeedbackUpdateLogService extends BaseService<FeedbackUpdateLog, Long> {

    /**
     * 根据类型和反馈ID查询
     *
     * @param type       类型
     * @param feedbackId 反馈ID
     * @param page       反馈ID
     * @return 操作日志
     */
    Page<FeedbackUpdateLog> findByTypeAndFeedbackId(Integer type, Long feedbackId, Pageable page);
}
